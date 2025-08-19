package gobov.roma.russia.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import gobov.roma.disruptor.BaseQuoteEvent;
import gobov.roma.disruptor.RussiaQuoteEvent;
import gobov.roma.disruptor.QuoteLevel;
import gobov.roma.russia.config.AlorProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AlorWebSocketClient extends WebSocketClient {

    private final TokenManager tokenManager;
    private final Disruptor<BaseQuoteEvent> disruptor;
    private final RingBuffer<BaseQuoteEvent> ringBuffer;
    private final ObjectMapper mapper;
    private final AlorProperties alorProperties;
    private final ExecutorService reconnectExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final Map<String, String> activeSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, String> instrumentGroups = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AlorWebSocketClient.class);
    private final ExecutorService virtualThreadExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> pingTask;
    private ScheduledFuture<?> cancelCheckTask;

    @Value("${alor.subscription.cancel-check-interval:2000}")
    private long checkCancelIntervalMs;

    @Value("classpath:config.properties")
    private Resource configResource;

    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 5000;

    @Autowired
    public AlorWebSocketClient(
            TokenManager tokenManager,
            Disruptor<BaseQuoteEvent> disruptor,
            ObjectMapper mapper,
            AlorProperties alorProperties,
            ExecutorService virtualThreadExecutor,
            ScheduledExecutorService scheduledExecutor) {
        super(createUri(alorProperties));
        this.tokenManager = tokenManager;
        this.disruptor = disruptor;
        this.ringBuffer = disruptor.getRingBuffer();
        this.mapper = mapper;
        this.alorProperties = alorProperties;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.scheduledExecutor = scheduledExecutor;
    }

    private static URI createUri(AlorProperties alorProperties) {
        if (alorProperties == null || alorProperties.getWebsocket() == null || alorProperties.getWebsocket().getUrl() == null) {
            throw new IllegalArgumentException("WebSocket URL is null or not configured in AlorProperties");
        }
        try {
            return URI.create(alorProperties.getWebsocket().getUrl());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid WebSocket URL: " + alorProperties.getWebsocket().getUrl(), e);
        }
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing AlorWebSocketClient");
        connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("WebSocket connected: {}", handshakedata.getHttpStatusMessage());
        schedulePing();
        scheduleCancelCheck();
    }

    @Override
    public void onMessage(String message) {
        logger.debug("Received WebSocket message in thread: {}", Thread.currentThread().getName());
        try {
            JsonNode node = mapper.readTree(message);
            processQuoteEvent(node);
        } catch (Exception e) {
            logger.error("Error parsing WebSocket message: {}", e.getMessage(), e);
        }
    }

    private void processQuoteEvent(JsonNode node) {
        try {
            String symbol = node.get("symbol").asText();
            String exchange = node.get("exchange").asText();
            long msTimestamp = node.get("timestamp").asLong();
            String guid = node.get("guid").asText(null);
            boolean snapshot = node.get("snapshot").asBoolean(false);

            List<QuoteLevel> bids = parseLevels(node.get("bids"));
            List<QuoteLevel> asks = parseLevels(node.get("asks"));

            ringBuffer.publishEvent((event, sequence) -> {
                if (event instanceof RussiaQuoteEvent russiaEvent) {
                    logger.debug("Publishing RussiaQuoteEvent for symbol: {} in thread: {}",
                            symbol, Thread.currentThread().getName());
                    russiaEvent.setSymbol(symbol);
                    russiaEvent.setExchange(exchange);
                    russiaEvent.setMsTimestamp(msTimestamp);
                    russiaEvent.setGuid(guid);
                    russiaEvent.setSnapshot(snapshot);
                    russiaEvent.setBids(bids);
                    russiaEvent.setAsks(asks);
                    russiaEvent.setTimestamp(msTimestamp / 1000);
                }
            });
            logger.info("Published RussiaQuoteEvent for symbol: {}", symbol);
        } catch (Exception e) {
            logger.error("Error processing quote event: {}", e.getMessage(), e);
        }
    }

    private List<QuoteLevel> parseLevels(JsonNode levelsNode) {
        List<QuoteLevel> levels = new ArrayList<>();
        if (levelsNode != null && levelsNode.isArray()) {
            for (JsonNode levelNode : levelsNode) {
                QuoteLevel level = new QuoteLevel();
                level.setPrice(new BigDecimal(levelNode.get("price").asText()));
                level.setVolume(levelNode.get("volume").asLong());
                levels.add(level);
            }
        }
        return levels;
    }

    private void schedulePing() {
        pingTask = scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                send("ping");
                logger.debug("Sent ping");
            } catch (Exception e) {
                logger.error("Ping error", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void scheduleCancelCheck() {
        cancelCheckTask = scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                checkSubscriptions();
            } catch (Exception e) {
                logger.error("Cancel check error", e);
            }
        }, 0, checkCancelIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void checkSubscriptions() {
        logger.debug("Checking active subscriptions");
        // Реализация проверки подписок (зависит от Alor API)
    }

    private void unsubscribeAll(String accessToken) {
        logger.info("Unsubscribing from all active subscriptions");
        activeSubscriptions.clear();
        // Реализация отписки (зависит от Alor API)
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        instrumentGroups.clear();
        activeSubscriptions.clear();
        logger.trace("Entering onClose()");
        logger.info("Connection closed: {} - {}", code, reason);

        if (code == 1006) {
            logger.error("Abnormal closure. Check token and connection params.");
        }

        if (reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS && shouldSubscribe()) {
            reconnectExecutor.submit(() -> {
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                    int attempt = reconnectAttempts.incrementAndGet();
                    logger.info("Reconnecting (attempt {})", attempt);
                    reconnect();
                } catch (Exception e) {
                    logger.error("Reconnect error", e);
                }
            });
        } else if (reconnectAttempts.get() >= MAX_RECONNECT_ATTEMPTS) {
            logger.error("Max reconnect attempts reached");
        }
        logger.trace("Exiting onClose()");
    }

    @Override
    public void onError(Exception ex) {
        logger.trace("Entering onError()");
        logger.error("WebSocket error", ex);
        logger.trace("Exiting onError()");
    }

    private boolean shouldSubscribe() {
        try (InputStream input = configResource.getInputStream()) {
            Properties props = new Properties();
            props.load(input);
            return Boolean.parseBoolean(props.getProperty("cancel.subscription", "true"));
        } catch (IOException e) {
            logger.error("Error reading config", e);
            return true;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (pingTask != null) pingTask.cancel(false);
        if (cancelCheckTask != null) cancelCheckTask.cancel(false);
        logger.trace("Entering shutdown()");
        logger.info("Shutting down WebSocket client");
        try {
            reconnectExecutor.shutdownNow();
            if (!activeSubscriptions.isEmpty()) {
                unsubscribeAll(tokenManager.getAccessToken());
            }
            close();
        } catch (Exception e) {
            logger.error("Shutdown error", e);
        }
        logger.trace("Exiting shutdown()");
    }
}