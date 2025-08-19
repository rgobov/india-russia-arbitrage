package gobov.roma.india.shoonya.service;

import gobov.roma.india.shoonya.controllers.QuoteController; // Исправлен импорт
import gobov.roma.india.shoonya.entity.QuoteEntity; // Исправлен импорт
import gobov.roma.india.shoonya.mapper.QuoteMapper; // Исправлен импорт
import gobov.roma.india.shoonya.repository.QuoteRepository; // Исправлен импорт
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

    @Autowired
    public QuoteService(QuoteRepository quoteRepository, QuoteMapper quoteMapper) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
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
}