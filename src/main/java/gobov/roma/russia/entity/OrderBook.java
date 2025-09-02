package gobov.roma.russia.entity;

import gobov.roma.reserch.TimeSlice;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
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
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slice_id")
    private TimeSlice timeSlice;

    @OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderBookLevel> bids;

    @OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderBookLevel> asks;
    }
