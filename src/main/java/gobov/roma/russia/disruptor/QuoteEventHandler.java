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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class QuoteEventHandler implements EventHandler<QuoteEvent> {

    private final OrderBookService orderBookService;
    Logger logger = LoggerFactory.getLogger(QuoteEventHandler.class);

    @Autowired
    public QuoteEventHandler(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @Override
    public void onEvent(QuoteEvent event, long sequence, boolean endOfBatch) {
        try {
            logger.info("Обработано событие: " + "в методе QuoteEvent объекта QuoteEventHandler");
            if (event == null) return;

            OrderBookDTO dto = new OrderBookDTO();
            dto.setSymbol(event.getSymbol());
            dto.setExchange(event.getExchange());
            dto.setTimestamp(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getMsTimestamp()),
                    ZoneId.systemDefault()
            ));

            // Маппинг уровней с конвертацией в BigDecimal
            dto.setBids(mapLevels(event.getBids()));
            dto.setAsks(mapLevels(event.getAsks()));
            logger.info("Обработано событие: " + dto.getSymbol() + " " + "в объекте QuoteEventHandler");

            orderBookService.saveOrderBook(dto);
        } catch (Exception e) {
            System.err.println("Ошибка обработки события: " + e.getMessage());
        } finally {
            event.clear();
        }
    }

    private List<LevelDTO> mapLevels(List<QuoteLevel> levels) {
        return levels.stream().map(level -> {
            LevelDTO dto = new LevelDTO();
            dto.setPrice(level.getPrice()); // Уже BigDecimal
            dto.setVolume(level.getVolume());
            return dto;
        }).collect(Collectors.toList());
    }
}