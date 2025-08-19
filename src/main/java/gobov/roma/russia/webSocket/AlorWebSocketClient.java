package gobov.roma.russia.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import gobov.roma.russia.disraptor.QuoteEvent;
import gobov.roma.russia.disraptor.QuoteLevel;
import gobov.roma.russia.config.AlorProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AlorWebSocketClient extends WebSocketClient {

    private final TokenManager tokenManager;
    private final Disruptor<QuoteEvent> disruptor;
    private final RingBuffer<QuoteEvent> ringBuffer;
    private final ObjectMapper mapper;
    private final AlorProperties alorProperties;
    private final ExecutorService reconnectExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final Map<String, String> activeSubscriptions = new ConcurrentHashMap<>(); // symbol -> guid
    private final Map<String, String> instrumentGroups = new ConcurrentHashMap<>(); // symbol -> group
    private static final Logger logger = LoggerFactory.getLogger(AlorWebSocketClient.class);
    private final ExecutorService virtualThreadExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> pingTask;
    private ScheduledFuture<?> cancelCheckTask;

    @org.springframework.beans.factory.annotation.Value("${alor.subscription.cancel-check-interval:2000}")
    private long checkCancelIntervalMs;

    @org.springframework.beans.factory.annotation.Value("classpath:config.properties")
    private Resource configResource;

    private Timer cancelCheckTimer;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 10000;

    @Autowired
    public AlorWebSocketClient(
            TokenManager tokenManager,
            Disruptor<QuoteEvent> disruptor,
            ObjectMapper mapper,
            AlorProperties alorProperties,
            ExecutorService virtualThreadExecutor,
            ScheduledExecutorService scheduledExecutor) {

        super(URI.create(alorProperties.getWebsocket().getUrl()));
        this.tokenManager = tokenManager;
        this.disruptor = disruptor;
        this.mapper = mapper;
        this.alorProperties = alorProperties;
        this.ringBuffer = disruptor.getRingBuffer();
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.scheduledExecutor = scheduledExecutor;

        addHeader("Origin", "https://alor.ru");
        addHeader("User-Agent", "Mozilla/5.0");
        setConnectionLostTimeout(30);
    }

    @PostConstruct
    public void init() {
        logger.trace("Entering init()");
        loadInstrumentConfig();
        logger.trace("Exiting init()");
    }

    private void loadInstrumentConfig() {
        logger.trace("Entering loadInstrumentConfig()");
        try (InputStream input = configResource.getInputStream()) {
            Properties props = new Properties();
            props.load(input);

            String instruments = props.getProperty("instruments", "");
            if (!instruments.isEmpty()) {
                Arrays.stream(instruments.split(","))
                        .map(String::trim)
                        .filter(pair -> pair.contains(":"))
                        .forEach(pair -> {
                            String[] parts = pair.split(":");
                            if (parts.length == 2) {
                                instrumentGroups.put(parts[0], parts[1]);
                            }
                        });
            }

            // Если в config.properties нет инструментов, используем дефолтные из YML
            if (instrumentGroups.isEmpty() && alorProperties.getMoexSecurities() != null) {
                instrumentGroups.putAll(alorProperties.getMoexSecurities());
            }

            logger.info("Loaded instruments: {}", instrumentGroups);
        } catch (IOException e) {
            logger.error("Error loading instrument config", e);
        }
        logger.trace("Exiting loadInstrumentConfig()");
    }

    public void connectToAlor() {
        logger.trace("Entering connectToAlor()");
        logger.info("Connecting to WebSocket: {}", alorProperties.getWebsocket().getUrl());
        super.connect();
        logger.trace("Exiting connectToAlor()");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.trace("Entering onOpen()");
        logger.info("Connected to ALOR WebSocket: {}", handshakedata.getHttpStatusMessage());
        String token = tokenManager.getAccessToken();
        if (token == null) {
            logger.error("Access Token unavailable. Closing connection.");
            close();
            logger.trace("Exiting onOpen()");
            return;
        }

        if (shouldSubscribe() && !activeSubscriptions.isEmpty()) {
            // Отписываемся от старых подписок
            unsubscribeAll(token);
        }

        if (shouldSubscribe() && instrumentGroups != null && !instrumentGroups.isEmpty()) {
            instrumentGroups.forEach((symbol, group) -> {
                String guid = UUID.randomUUID().toString();
                activeSubscriptions.put(symbol, guid);

                String subscribeMessage = String.format(
                        "{\"opcode\":\"OrderBookGetAndSubscribe\",\"code\":\"%s\",\"depth\":5," +
                                "\"exchange\":\"MOEX\",\"group\":\"%s\",\"format\":\"Simple\"," +
                                "\"frequency\":0,\"guid\":\"%s\",\"token\":\"%s\"}",
                        symbol, group, guid, token
                );
                logger.debug("Sending subscription: {}", subscribeMessage);
                send(subscribeMessage);
            });
        }

        // Пинги
        pingTask = scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                try {
                    sendPing();
                    logger.debug("Ping sent");
                } catch (Exception e) {
                    logger.error("Error sending ping", e);
                }
            }
        }, 30000, 30000, TimeUnit.MILLISECONDS);

        cancelCheckTask = scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                boolean shouldSub = shouldSubscribe();
                if (!shouldSub && !activeSubscriptions.isEmpty()) {
                    unsubscribeAll(tokenManager.getAccessToken());
                }
            }
        }, checkCancelIntervalMs, checkCancelIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void unsubscribeAll(String token) {
        logger.trace("Entering unsubscribeAll()");
        activeSubscriptions.forEach((symbol, guid) -> {
            String unsubscribeMessage = String.format(
                    "{\"opcode\":\"unsubscribe\",\"guid\":\"%s\",\"token\":\"%s\"}",
                    guid, token
            );
            logger.debug("Unsubscribing: {}", unsubscribeMessage);
            send(unsubscribeMessage);
        });
        activeSubscriptions.clear();
        logger.trace("Exiting unsubscribeAll()");
    }

    @Override
    public void onMessage(String message) {
        logger.trace("Entering onMessage()");
        logger.debug("Received WebSocket message: {}", message);

        try {
            JsonNode node = mapper.readTree(message);

            // Обработка ответов на команды (subscribe/unsubscribe)
            if (node.has("httpCode")) {
                int httpCode = node.get("httpCode").asInt();
                String requestGuid = node.has("requestGuid") ? node.get("requestGuid").asText() : "unknown";
                logger.debug("HTTP {} for request: {}", httpCode, requestGuid);

                if (httpCode == 200) {
                    if (message.contains("unsubscribe")) {
                        logger.debug("Unsubscribed: {}", requestGuid);
                        activeSubscriptions.values().remove(requestGuid);
                    }
                } else if (httpCode == 400) {
                    logger.error("Bad request: {}", message);
                } else if (httpCode == 401) {
                    logger.error("Auth error: {}", message);
                    tokenManager.refreshAccessToken();
                } else {
                    logger.error("Unknown error: {}", message);
                }
            }
            // Обработка сообщений с котировками
            else if (node.has("data") && node.has("guid")) {
                logger.debug("Processing quote event for GUID: {}", node.get("guid").asText());
                processQuoteEvent(node);
            }
            // Обработка неподдерживаемых форматов
            else {
                logger.warn("Unsupported message format: {}", message);
            }
        } catch (Exception e) {
            logger.error("Message processing error", e);
        }
        logger.trace("Exiting onMessage()");
    }

    private void processQuoteEvent(JsonNode node) {
        logger.trace("Entering processQuoteEvent()");
        JsonNode data = node.get("data");
        if (data == null || !data.has("bids") || !data.has("asks")) return;

        // Получаем GUID события
        String guid = node.has("guid") ? node.get("guid").asText() : null;
        if (guid == null) {
            logger.warn("GUID not found in event");
            return;
        }

        // Находим символ по GUID из активных подписок
        String symbol = activeSubscriptions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(guid))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");

        if (symbol.isEmpty()) {
            logger.warn("Symbol not found for GUID: {}", guid);
            return;
        }

        boolean snapshot = data.has("snapshot") && data.get("snapshot").asBoolean();
        boolean existing = !data.has("existing") || data.get("existing").asBoolean();
        long timestamp = data.has("timestamp") ? data.get("timestamp").asLong() : 0L;
        long msTimestamp = data.has("ms_timestamp") ? data.get("ms_timestamp").asLong() : System.currentTimeMillis();
        String exchange = "MOEX"; // Явное указание биржи

        List<QuoteLevel> bids = convertToLevels(data.get("bids"));
        List<QuoteLevel> asks = convertToLevels(data.get("asks"));

        if (bids.isEmpty() && asks.isEmpty()) return;

        long sequence = ringBuffer.next();
        try {
            QuoteEvent event = ringBuffer.get(sequence);
            event.setSymbol(symbol);
            event.setExchange(exchange);
            event.setSnapshot(snapshot);
            event.setExisting(existing);
            event.setTimestamp(timestamp);
            event.setMsTimestamp(msTimestamp);
            event.setGuid(guid);
            event.setBids(bids);
            event.setAsks(asks);
            logger.debug("QuoteEvent published for: {}", symbol);
        } finally {
            ringBuffer.publish(sequence);
        }
        logger.trace("Exiting processQuoteEvent()");
    }

    // AlorWebSocketClient.java (только метод convertToLevels)
    private List<QuoteLevel> convertToLevels(JsonNode levelsNode) {
        logger.trace("Entering convertToLevels()");
        if (levelsNode == null || !levelsNode.isArray() || levelsNode.isEmpty()) {
            return Collections.emptyList();
        }

        List<QuoteLevel> levels = new ArrayList<>(levelsNode.size());
        levelsNode.forEach(node -> {
            QuoteLevel level = new QuoteLevel();

            // Парсим цену как BigDecimal из строки
            String priceStr = node.get("price").asText();
            try {
                level.setPrice(new BigDecimal(priceStr));
            } catch (NumberFormatException e) {
                logger.error("Error parsing price: {}", priceStr, e);
                level.setPrice(BigDecimal.ZERO);
            }

            level.setVolume(node.get("volume").asLong());
            levels.add(level);
        });
        logger.trace("Exiting convertToLevels() with {} levels", levels.size());
        return levels;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        instrumentGroups.clear();
        activeSubscriptions.clear();
        logger.trace("Entering onClose()");
        logger.info("Connection closed: {} - {}", code, reason);
        activeSubscriptions.clear();

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
            if (cancelCheckTimer != null) cancelCheckTimer.cancel();
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