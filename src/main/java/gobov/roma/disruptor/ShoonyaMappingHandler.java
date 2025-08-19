package gobov.roma.disruptor;

import com.lmax.disruptor.EventHandler;
import gobov.roma.india.shoonya.entity.QuoteEntity;
import gobov.roma.india.shoonya.mapper.QuoteMapper;
import gobov.roma.india.shoonya.controllers.QuoteController.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShoonyaMappingHandler implements EventHandler<BaseQuoteEvent> {

    private final QuoteMapper quoteMapper;

    @Autowired
    public ShoonyaMappingHandler(QuoteMapper quoteMapper) {
        this.quoteMapper = quoteMapper;
    }

    @Override
    public void onEvent(BaseQuoteEvent event, long sequence, boolean endOfBatch) {
        if (!(event instanceof ShoonyaQuoteEvent shoonyaEvent)) {
            return; // Пропустить, если не shoonya
        }

        List<Quote> quotes = shoonyaEvent.getQuotes();
        if (quotes == null || quotes.isEmpty()) return;

        List<QuoteEntity> entities = quotes.stream()
                .map(quoteMapper::toEntity)
                .collect(Collectors.toList());

        shoonyaEvent.setEntities(entities);
    }
}