package gobov.roma.russia.webSocket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class TokenManager {

    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    @Value("${alor.refresh.token}")
    private String refreshToken;

    @Value("${alor.refresh.url}")
    private String refreshUrl;

    @Value("${alor.refresh.interval:1440000}") // 24 минуты
    private long refreshInterval;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private String accessToken;
    private final ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> refreshTask;

    @Autowired
    public TokenManager(ScheduledExecutorService scheduledExecutor) {
        this.scheduledExecutor = scheduledExecutor;
    }

    @PostConstruct
    public void init() {
        refreshAccessToken();
        startTokenRefreshTimer();
    }

    public void refreshAccessToken() {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                String requestBody = "{\"token\": \"" + refreshToken + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(refreshUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    // Упрощенный парсинг (реализуйте надежный парсинг JSON)
                    int start = responseBody.indexOf("\"AccessToken\":\"") + 15;
                    int end = responseBody.indexOf("\"", start);
                    accessToken = responseBody.substring(start, end);
                    return;
                }
            } catch (Exception e) {
                retries++;
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Не удалось получить токен доступа");
    }

    private void startTokenRefreshTimer() {
        refreshTask = scheduledExecutor.scheduleAtFixedRate(
                () -> {
                    try {
                        refreshAccessToken();
                        logger.info("Access token refreshed successfully");
                    } catch (Exception e) {
                        logger.error("Failed to refresh access token", e);
                        // Дополнительная логика обработки ошибки при необходимости
                    }
                },
                refreshInterval,
                refreshInterval,
                TimeUnit.MILLISECONDS
        );
    }

    @PreDestroy
    public void destroy() {
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }
}