package gobov.roma.reserch;

import gobov.roma.russia.dto.OrderBookDTO;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TimeSlice {
    private final ConcurrentHashMap<String, OrderBookDTO> timeSlice = new ConcurrentHashMap<>(1000);
    private static final int MAX_LEVELS = 10;

    public OrderBookDTO getOrCreateOrderBook(String symbol) {
        return timeSlice.computeIfAbsent(symbol, k -> new OrderBookDTO(MAX_LEVELS));
    }

    public OrderBookDTO getOrderBook(String symbol) {
        return timeSlice.get(symbol);
    }
}