package gobov.roma.russia.service;

import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.entity.OrderBook;
import gobov.roma.russia.entity.OrderBookLevel;
import gobov.roma.russia.repository.OrderBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OutOrderBookServise {

    private final OrderBookRepository orderBookRepository;

    @Autowired
    public OutOrderBookServise(OrderBookRepository orderBookRepository) {
        this.orderBookRepository = orderBookRepository;
    }

    public List<OrderBookDTO> convertOrderBookToDTO(List<String> symbols) {
        List<OrderBook> orderBooks = orderBookRepository.findLatestBySymbols(symbols);
        return orderBooks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderBookDTO convertToDTO(OrderBook orderBook) {
        if (orderBook == null) {
            return null;
        }

        OrderBookDTO dto = new OrderBookDTO();
        dto.symbol = orderBook.getSymbol(); // Прямое присваивание полю
        dto.exchange = orderBook.getExchange(); // Прямое присваивание полю
        dto.timestamp = orderBook.getTimestamp(); // Прямое присваивание полю

        // Преобразуем списки в массивы
        List<OrderBookLevel> bidLevels = orderBook.getBidLevels();
        dto.bids = new LevelDTO[bidLevels.size()];
        dto.bidCount = bidLevels.size();
        for (int i = 0; i < bidLevels.size(); i++) {
            dto.bids[i] = convertLevelToDTO(bidLevels.get(i));
        }

        List<OrderBookLevel> askLevels = orderBook.getAskLevels();
        dto.asks = new LevelDTO[askLevels.size()];
        dto.askCount = askLevels.size();
        for (int i = 0; i < askLevels.size(); i++) {
            dto.asks[i] = convertLevelToDTO(askLevels.get(i));
        }

        return dto;
    }

    private LevelDTO convertLevelToDTO(OrderBookLevel level) {
        LevelDTO dto = new LevelDTO();
        dto.price = level.getPrice(); // Прямое присваивание полю
        dto.volume = level.getVolume(); // Прямое присваивание полю
        return dto;
    }
}