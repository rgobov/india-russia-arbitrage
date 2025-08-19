package gobov.roma.india.shoonya.mapper;

import gobov.roma.india.shoonya.controllers.QuoteController; // Исправлен импорт
import gobov.roma.india.shoonya.entity.QuoteEntity; // Исправлен импорт
import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    public QuoteEntity toEntity(QuoteController.Quote quoteDto) {
        QuoteEntity entity = new QuoteEntity();
        entity.setExchange(quoteDto.getExchange());
        entity.setSymbol(quoteDto.getSymbol());
        entity.setExpDate(quoteDto.getExpDate());
        entity.setStrikePrice(quoteDto.getStrikePrice());
        entity.setOpType(quoteDto.getOpType());
        entity.setBidPrice(quoteDto.getBidPrice());
        entity.setBidQty(quoteDto.getBidQty());
        entity.setAskPrice(quoteDto.getAskPrice());
        entity.setAskQty(quoteDto.getAskQty());
        return entity;
    }
}