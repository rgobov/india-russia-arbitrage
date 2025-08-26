package gobov.roma.india.shoonya.service;

import gobov.roma.india.shoonya.controllers.QuoteController; // Исправлен импорт
import gobov.roma.india.shoonya.entity.QuoteEntity; // Исправлен импорт
import gobov.roma.india.shoonya.mapper.QuoteMapper; // Исправлен импорт
import gobov.roma.india.shoonya.repository.QuoteRepository; // Исправлен импорт
import gobov.roma.reserch.TimeSliceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteService {

    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;
    private final TimeSliceDTO timeSliceDTO;

    @Autowired
    public QuoteService(QuoteRepository quoteRepository, QuoteMapper quoteMapper, TimeSliceDTO timeSliceDTO) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
        this.timeSliceDTO = timeSliceDTO;
    }

    @Transactional
    public int saveQuotes(List<QuoteController.Quote> quotes) {
        List<QuoteEntity> entities = quotes.stream()
                .map(quoteMapper::toEntity)
                .collect(Collectors.toList());

        List<QuoteEntity> savedEntities = quoteRepository.saveAll(entities);
        logger.info("Сохранено {} котировок", savedEntities.size());
        return savedEntities.size();
    }

    public void saveToMap(List<QuoteController.Quote> quotes){
        quotes.forEach((quote) -> {
            QuoteController.Quote quoteFromMap = timeSliceDTO.getOrCreateOrderBookIndia(quote.getSymbol());
            quoteFromMap.setBidPrice(quote.getBidPrice());
            quoteFromMap.setAskPrice(quote.getAskPrice());
            quoteFromMap.setBidQty(quote.getBidQty());
            quoteFromMap.setAskQty(quote.getAskQty());
            if(quote.getSymbol() == null) {
                quoteFromMap.setSymbol(quote.getSymbol());
                quoteFromMap.setExchange(quote.getExchange());
                quoteFromMap.setOpType(quote.getOpType());
                quoteFromMap.setStrikePrice(quote.getStrikePrice());
                quoteFromMap.setExpDate(quote.getExpDate());
            }

        });
    }


}