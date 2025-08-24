package gobov.roma.russia.disruptor;

import com.lmax.disruptor.EventHandler;
import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.service.OrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class QuoteEventHandler implements EventHandler<QuoteEvent> {
    private final OrderBookService orderBookService;
    private static final Logger logger = LoggerFactory.getLogger(QuoteEventHandler.class);

    private static final int MAX_LEVELS = 10;
    private final ThreadLocal<OrderBookDTO> dtoThreadLocal =
            ThreadLocal.withInitial(() -> new OrderBookDTO(MAX_LEVELS));

    @Autowired
    public QuoteEventHandler(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public final void onEvent(QuoteEvent event, long sequence, boolean endOfBatch) {
        try {
            if (event == null || event.bidCount == 0 && event.askCount == 0) return;

            OrderBookDTO dto = dtoThreadLocal.get();
            dto.clear();

            dto.symbol = event.symbol;
            dto.exchange = event.exchange;
            dto.timestamp = Instant.ofEpochMilli(event.msTimestamp);

            // Копируем биды
            dto.bidCount = event.bidCount;
            for (int i = 0; i < event.bidCount; i++) {
                dto.bids[i].set(event.bids[i].price, event.bids[i].volume);
            }

            // Копируем аски
            dto.askCount = event.askCount;
            for (int i = 0; i < event.askCount; i++) {
                dto.asks[i].set(event.asks[i].price, event.asks[i].volume);
            }

            orderBookService.saveOrderBook(dto);
        } catch (Exception e) {
            logger.error("Ошибка обработки события", e);
        } finally {
            if (event != null) {
                event.clear();
            }
        }
    }
}