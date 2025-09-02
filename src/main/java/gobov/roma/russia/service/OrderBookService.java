package gobov.roma.russia.service;

import gobov.roma.reserch.TimeSliceDTO;
import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.entity.OrderBook;
import gobov.roma.russia.entity.OrderBookLevel;
import gobov.roma.russia.repository.OrderBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class OrderBookService {

    private final OrderBookRepository orderBookRepository;
    private final ExecutorService virtualThreadExecutor;
    private final TimeSliceDTO timeSliceDTO;

    @Autowired
    public OrderBookService(
            OrderBookRepository orderBookRepository,
            ExecutorService virtualThreadExecutor, TimeSliceDTO timeSliceDTO) {
        this.orderBookRepository = orderBookRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.timeSliceDTO = timeSliceDTO;
    }

    public void saveOrderBook(OrderBookDTO dto) {
        timeSliceDTO.updateMoexOrderBook(dto);
    }

//    public void saveOrderBook(OrderBookDTO dto) {
//       saveOrderBookTransactional(dto);
//    }
//    @Transactional
//    public void saveOrderBookTransactional(OrderBookDTO dto) {
//        virtualThreadExecutor.execute(() -> {
//            OrderBook orderBook = mapDtoToEntity(dto);
//            orderBookRepository.save(orderBook);
//        });
//    }

    private OrderBook mapDtoToEntity(OrderBookDTO dto) {
        OrderBook entity = new OrderBook();
        entity.setSymbol(dto.symbol);
        entity.setExchange(dto.exchange);
        entity.setTimestamp(dto.timestamp);

        // Для бидов
        List<OrderBookLevel> bidLevels = new ArrayList<>(dto.bidCount);
        for (int i = 0; i < dto.bidCount; i++) {
            LevelDTO levelDto = dto.bids[i];
            OrderBookLevel level = new OrderBookLevel();
            level.setPrice(levelDto.price);
            level.setVolume(levelDto.volume);
            level.setBid(true);
            bidLevels.add(level);
        }
        entity.setBids(bidLevels);

        // Для асков
        List<OrderBookLevel> askLevels = new ArrayList<>(dto.askCount);
        for (int i = 0; i < dto.askCount; i++) {
            LevelDTO levelDto = dto.asks[i];
            OrderBookLevel level = new OrderBookLevel();
            level.setPrice(levelDto.price);
            level.setVolume(levelDto.volume);
            level.setBid(false);
            askLevels.add(level);
        }
        entity.setAsks(askLevels);

        return entity;
    }

}