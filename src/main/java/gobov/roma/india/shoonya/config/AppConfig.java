package gobov.roma.india.shoonya.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Важная настройка: парсить числа как BigDecimal (а не Double)
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        return mapper;
    }
}