package gobov.roma.disruptor;

import gobov.roma.india.shoonya.controllers.QuoteController.Quote;
import gobov.roma.india.shoonya.entity.QuoteEntity;
import lombok.Data;

import java.util.List;

@Data
public class ShoonyaQuoteEvent extends BaseQuoteEvent {

    private List<Quote> quotes;
    private List<QuoteEntity> entities; // Добавлено поле для хранения маппированных сущностей

    public ShoonyaQuoteEvent() {
        setType(EventType.SHOONYA);
    }

    @Override
    public void clear() {
        if (quotes != null) quotes.clear();
        if (entities != null) entities.clear(); // Очистка entities
    }
}