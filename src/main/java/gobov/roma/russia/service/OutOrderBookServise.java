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
        dto.setSymbol(orderBook.getSymbol());
        dto.setExchange(orderBook.getExchange());
        dto.setTimestamp(orderBook.getTimestamp());

        // Используем предварительно отфильтрованные уровни
        dto.setBids(orderBook.getBidLevels().stream()
                .map(this::convertLevelToDTO)
                .collect(Collectors.toList()));

        dto.setAsks(orderBook.getAskLevels().stream()
                .map(this::convertLevelToDTO)
                .collect(Collectors.toList())); // Исправлено на collect

        return dto;
    }

    private LevelDTO convertLevelToDTO(OrderBookLevel level) {
        LevelDTO dto = new LevelDTO();
        dto.setPrice(level.getPrice());
        dto.setVolume(level.getVolume());
        return dto;
    }
}