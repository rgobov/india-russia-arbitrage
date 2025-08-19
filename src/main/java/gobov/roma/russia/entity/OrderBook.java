package gobov.roma.russia.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "order_books")
@Data
public class OrderBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String exchange;
    private LocalDateTime timestamp;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_book_id")
    private List<OrderBookLevel> bids;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_book_id")
    private List<OrderBookLevel> asks;

    public List<OrderBookLevel> getBidLevels() {
        return bids.stream()
                .filter(OrderBookLevel::isBid)
                .collect(Collectors.toList());
    }

    public List<OrderBookLevel> getAskLevels() {
        return asks.stream()
                .filter(level -> !level.isBid())
                .collect(Collectors.toList());
    }
}