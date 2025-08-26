package gobov.roma.reserch;

import gobov.roma.india.shoonya.controllers.QuoteController;
import gobov.roma.russia.dto.OrderBookDTO;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TimeSliceDTO {

    // Блок обработки котировок Московской биржи.
    private final ConcurrentHashMap<String, OrderBookDTO> timeSliceMOEX = new ConcurrentHashMap<>(1000);
    private static final int MAX_LEVELS_MOEX = 10;

    public OrderBookDTO getOrCreateOrderBookMoex(String symbol) {
        return timeSliceMOEX.computeIfAbsent(symbol, k -> new OrderBookDTO(MAX_LEVELS_MOEX));
    }

    // Блок обработки котировок Индийских бирж.
    private final ConcurrentHashMap<String, QuoteController.Quote> timeSliceIndia = new ConcurrentHashMap<>(100);

    public QuoteController.Quote getOrCreateOrderBookIndia(String symbol) {
        return timeSliceIndia.computeIfAbsent(symbol, k -> new QuoteController.Quote());
    }


}