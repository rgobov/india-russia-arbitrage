package gobov.roma.reserch;

import gobov.roma.india.shoonya.entity.QuoteEntity;
import gobov.roma.russia.entity.OrderBook;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// TimeSlice.java
@Entity
@Table(name = "time_slices")
@Data
public class TimeSlice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant timestamp = Instant.now();

    // Связь с OrderBook
    @OneToMany(mappedBy = "timeSlice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderBook> orderBooks = new ArrayList<>();

    // Связь с QuoteEntity (если есть)
    @OneToMany(mappedBy = "timeSlice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteEntity> quotes = new ArrayList<>();
}