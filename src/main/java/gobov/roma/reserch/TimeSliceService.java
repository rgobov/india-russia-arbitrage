package gobov.roma.reserch;

import gobov.roma.india.shoonya.controllers.QuoteController;
import gobov.roma.india.shoonya.entity.QuoteEntity;
import gobov.roma.india.shoonya.mapper.QuoteMapper;
import gobov.roma.india.shoonya.repository.QuoteRepository;
import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.dto.LevelDTO;
import gobov.roma.russia.entity.OrderBook;
import gobov.roma.russia.entity.OrderBookLevel;
import gobov.roma.russia.repository.OrderBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TimeSliceService {

    @Autowired
    private TimeSliceDTO timeSliceDTO;

    @Autowired
    private TimeSliceRepository timeSliceRepository;

    @Autowired
    private QuoteMapper quoteMapper;

    @Autowired
    private OrderBookRepository orderBookRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    // Периодическое создание среза (каждые 30 секунд)
    // @Scheduled(fixedRate = 30000)
    @Transactional
    public void createSnapshot() {
        TimeSlice slice = new TimeSlice();
        slice.setTimestamp(Instant.now());

        // Копируем MOEX order books и India quotes
        List<OrderBook> moexBooks = copyMoexToEntities(slice);
        List<QuoteEntity> indiaQuotes = copyIndiaToEntities(slice);

        // Устанавливаем связи в TimeSlice
        slice.getOrderBooks().addAll(moexBooks);
        slice.getQuotes().addAll(indiaQuotes);

        // Каскадное сохранение через TimeSlice
        timeSliceRepository.save(slice);
    }

    // Метод для копирования MOEX
    private List<OrderBook> copyMoexToEntities(TimeSlice slice) {
        List<OrderBook> books = new ArrayList<>();
        for (Map.Entry<String, OrderBookDTO> entry : timeSliceDTO.timeSliceMOEX.entrySet()) {
            OrderBookDTO dto = entry.getValue();
            OrderBook entity = new OrderBook();
            entity.setSymbol(dto.symbol);
            entity.setExchange(dto.exchange);
            entity.setTimestamp(dto.timestamp != null ? dto.timestamp : Instant.now());
            entity.setTimeSlice(slice); // Устанавливаем объект TimeSlice

            // Bids
            List<OrderBookLevel> bids = new ArrayList<>();
            for (int i = 0; i < dto.bidCount; i++) {
                LevelDTO levelDto = dto.bids[i];
                if (levelDto.price != null) {
                    OrderBookLevel level = new OrderBookLevel();
                    level.setPrice(levelDto.price);
                    level.setVolume(levelDto.volume);
                    level.setBid(true);
                    level.setOrderBook(entity); // Устанавливаем обратную связь
                    bids.add(level);
                }
            }
            entity.setBids(bids);

            // Asks
            List<OrderBookLevel> asks = new ArrayList<>();
            for (int i = 0; i < dto.askCount; i++) {
                LevelDTO levelDto = dto.asks[i];
                if (levelDto.price != null) {
                    OrderBookLevel level = new OrderBookLevel();
                    level.setPrice(levelDto.price);
                    level.setVolume(levelDto.volume);
                    level.setBid(false);
                    level.setOrderBook(entity); // Устанавливаем обратную связь
                    asks.add(level);
                }
            }
            entity.setAsks(asks);

            books.add(entity);
        }
        return books;
    }

    // Метод для копирования India
    private List<QuoteEntity> copyIndiaToEntities(TimeSlice slice) {
        List<QuoteEntity> quotes = new ArrayList<>();
        for (Map.Entry<String, QuoteController.Quote> entry : timeSliceDTO.timeSliceIndia.entrySet()) {
            QuoteController.Quote dto = entry.getValue();
            QuoteEntity entity = quoteMapper.toEntity(dto);
            entity.setTimeSlice(slice); // Устанавливаем объект TimeSlice
            quotes.add(entity);
        }
        return quotes;
    }

    // Метод для извлечения среза по ID
    public TimeSlice getSliceById(Long id) {
        return timeSliceRepository.findById(id).orElse(null);
    }

    // Метод для извлечения последнего среза
    public TimeSlice getLatestSlice() {
        return timeSliceRepository.findFirstByOrderByTimestampDesc();
    }

    // Метод для извлечения срезов после определённого времени
    public List<TimeSlice> getSlicesAfter(Instant startTime) {
        return timeSliceRepository.findByTimestampAfter(startTime);
    }
}