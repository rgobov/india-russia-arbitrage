package gobov.roma.russia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class OrderBookLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 20, scale = 10) // Точность 20 знаков, 10 после запятой
    private BigDecimal price;
    private long volume;
    private boolean isBid; // true для BID, false для ASK
}