package gobov.roma.russia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "alor")
public class AlorProperties {
    private Map<String, String> moexSecurities;
    private Websocket websocket;
    private Refresh refresh;
    private Subscription subscription;

    @Getter @Setter
    public static class Websocket {
        private String url;
    }

    @Getter @Setter
    public static class Refresh {
        private String url;
        private String token;
        private long interval;
    }

    @Getter @Setter
    public static class Subscription {
        private long cancelCheckInterval;
        private boolean enabled;
    }

    // Удален класс Instrument, т.к. инструменты теперь берутся из конфига
}