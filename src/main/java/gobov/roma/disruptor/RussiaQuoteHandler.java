package gobov.roma.disruptor;

import com.lmax.disruptor.EventHandler;
import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.service.OrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RussiaQuoteHandler implements EventHandler<BaseQuoteEvent> {

    private final OrderBookService orderBookService;
    private static final Logger logger = LoggerFactory.getLogger(RussiaQuoteHandler.class);

    @Autowired
    public RussiaQuoteHandler(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public void onEvent(BaseQuoteEvent event, long sequence, boolean endOfBatch) {
        if (!(event instanceof RussiaQuoteEvent russiaEvent)) {
            logger.debug("Skipping non-Russia event in RussiaQuoteHandler, sequence: {}", sequence);
            return; // Пропустить, если не RussiaQuoteEvent
        }

        try {
            logger.debug("Processing Russia event in Disruptor thread: {}, sequence: {}",
                    Thread.currentThread().getName(), sequence);

            OrderBookDTO dto = new OrderBookDTO();
            dto.setSymbol(russiaEvent.getSymbol());
            dto.setExchange(russiaEvent.getExchange());
            dto.setTimestamp(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(russiaEvent.getMsTimestamp()),
                    ZoneId.systemDefault()
            ));

            dto.setBids(mapLevels(russiaEvent.getBids()));
            dto.setAsks(mapLevels(russiaEvent.getAsks()));

            orderBookService.saveOrderBook(dto); // Сохранение в виртуальном потоке через OrderBookService
            logger.info("Processed Russia event for symbol: {}", russiaEvent.getSymbol());
        } catch (Exception e) {
            logger.error("Ошибка обработки Russia события: {}", e.getMessage(), e);
        } finally {
            event.clear();
            logger.debug("Cleared Russia event, sequence: {}", sequence);
        }
    }

    private List<LevelDTO> mapLevels(List<QuoteLevel> levels) {
        if (levels == null) return List.of(); // Защита от null
        return levels.stream().map(level -> {
            LevelDTO dto = new LevelDTO();
            dto.setPrice(level.getPrice());
            dto.setVolume(level.getVolume());
            return dto;
        }).collect(Collectors.toList());
    }
}