package gobov.roma.russia.disruptor;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteLevel {
    private BigDecimal price;
    private long volume; // Изменено с int на long
}