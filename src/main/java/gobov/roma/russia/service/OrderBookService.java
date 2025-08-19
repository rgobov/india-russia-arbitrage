package gobov.roma.russia.service;

import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.entity.OrderBook;
import gobov.roma.russia.entity.OrderBookLevel;
import gobov.roma.russia.repository.OrderBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class OrderBookService {

    private final OrderBookRepository orderBookRepository;
    private final ExecutorService virtualThreadExecutor;

    @Autowired
    public OrderBookService(
            OrderBookRepository orderBookRepository,
            ExecutorService virtualThreadExecutor) {
        this.orderBookRepository = orderBookRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }


    public void saveOrderBook(OrderBookDTO dto) {
       saveOrderBookTransactional(dto);
    }
    @Transactional
    public void saveOrderBookTransactional(OrderBookDTO dto) {
        virtualThreadExecutor.execute(() -> {
            OrderBook orderBook = mapDtoToEntity(dto);
            orderBookRepository.save(orderBook);
        });
    }

    private OrderBook mapDtoToEntity(OrderBookDTO dto) {
        OrderBook entity = new OrderBook();
        entity.setSymbol(dto.getSymbol());
        entity.setExchange(dto.getExchange());
        entity.setTimestamp(dto.getTimestamp());

        // Для бидов устанавливаем isBid = true
        entity.setBids(dto.getBids().stream()
                .map(levelDto -> {
                    OrderBookLevel level = mapLevelDto(levelDto);
                    level.setBid(true); // Устанавливаем флаг
                    return level;
                })
                .collect(Collectors.toList()));

        // Для асков устанавливаем isBid = false
        entity.setAsks(dto.getAsks().stream()
                .map(levelDto -> {
                    OrderBookLevel level = mapLevelDto(levelDto);
                    level.setBid(false); // Устанавливаем флаг
                    return level;
                })
                .collect(Collectors.toList()));

        return entity;
    }

    private OrderBookLevel mapLevelDto(LevelDTO dto) {
        OrderBookLevel level = new OrderBookLevel();
        level.setPrice(dto.getPrice());
        level.setVolume(dto.getVolume());
        // Поле isBid будет установлено в mapDtoToEntity
        return level;
    }
}