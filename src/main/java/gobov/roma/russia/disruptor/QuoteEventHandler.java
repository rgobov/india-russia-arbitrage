package gobov.roma.russia.disruptor;

import com.lmax.disruptor.EventHandler;
import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.service.OrderBookService;
import gobov.roma.reserch.TimeSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class QuoteEventHandler implements EventHandler<QuoteEvent> {
    private final OrderBookService orderBookService;
    private final TimeSlice timeSlice;
    private static final Logger logger = LoggerFactory.getLogger(QuoteEventHandler.class);

    @Autowired
    public QuoteEventHandler(OrderBookService orderBookService, TimeSlice timeSlice) {
        this.orderBookService = orderBookService;
        this.timeSlice = timeSlice;
    }

    @Override
    public final void onEvent(QuoteEvent event, long sequence, boolean endOfBatch) {
        try {
            if (event == null || (event.bidCount == 0 && event.askCount == 0)) return;

            // Получаем или создаем OrderBookDTO для символа
            OrderBookDTO dto = timeSlice.getOrCreateOrderBook(event.symbol);

            // Атомарно обновляем стакан
            updateOrderBook(dto, event);

            // Для сервиса сохраняем ссылку на актуальный объект
            orderBookService.saveOrderBook(dto);
        } catch (Exception e) {
            logger.error("Ошибка обработки события", e);
        } finally {
            if (event != null) {
                event.clear();
            }
        }
    }

    private void updateOrderBook(OrderBookDTO dto, QuoteEvent event) {
        dto.symbol = event.symbol;
        dto.exchange = event.exchange;
        dto.timestamp = Instant.ofEpochMilli(event.msTimestamp);

        // Обновляем биды
        dto.bidCount = event.bidCount;
        for (int i = 0; i < event.bidCount; i++) {
            LevelDTO bid = dto.bids[i];
            LevelDTO eventBid = event.bids[i];
            bid.price = eventBid.price;
            bid.volume = eventBid.volume;
        }

        // Обновляем аски
        dto.askCount = event.askCount;
        for (int i = 0; i < event.askCount; i++) {
            LevelDTO ask = dto.asks[i];
            LevelDTO eventAsk = event.asks[i];
            ask.price = eventAsk.price;
            ask.volume = eventAsk.volume;
        }
    }
}