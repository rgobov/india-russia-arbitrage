package gobov.roma.disruptor;

import lombok.Data;
import java.util.List;

@Data
public class RussiaQuoteEvent extends BaseQuoteEvent {
    private String symbol;
    private String exchange;
    private boolean snapshot;
    private List<QuoteLevel> bids; // Изменено с Level на QuoteLevel
    private List<QuoteLevel> asks; // Изменено с Level на QuoteLevel
    private long timestamp;
    private long msTimestamp;
    private boolean existing;
    private String guid;

    public void clear() {
        symbol = null;
        exchange = null;
        snapshot = false;
        if (bids != null) bids.clear();
        if (asks != null) asks.clear();
        timestamp = 0L;
        msTimestamp = 0L;
        existing = false;
        guid = null;
    }
}