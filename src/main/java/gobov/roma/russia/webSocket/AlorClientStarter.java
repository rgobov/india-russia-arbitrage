package gobov.roma.russia.webSocket;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AlorClientStarter {

    private final AlorWebSocketClient webSocketClient;

    public AlorClientStarter(AlorWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @PostConstruct
    public void start() {
        // Можно добавить логирование или другую инициализацию, если нужно
    }
}