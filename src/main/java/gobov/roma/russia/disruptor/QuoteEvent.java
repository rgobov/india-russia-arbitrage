package gobov.roma.russia.disruptor;

public final class QuoteEvent {
    public String symbol;
    public String exchange;
    public boolean snapshot;
    public QuoteLevel[] bids;
    public QuoteLevel[] asks;
    public int bidCount;
    public int askCount;
    public long timestamp;
    public long msTimestamp;
    public boolean existing;
    public String guid;

    public QuoteEvent(int maxLevels) {
        bids = new QuoteLevel[maxLevels];
        asks = new QuoteLevel[maxLevels];
        for (int i = 0; i < maxLevels; i++) {
            bids[i] = new QuoteLevel();
            asks[i] = new QuoteLevel();
        }
    }

    public void clear() {
        symbol = null;
        exchange = null;
        snapshot = false;
        bidCount = 0;
        askCount = 0;
        timestamp = 0L;
        msTimestamp = 0L;
        existing = false;
        guid = null;
        if (bids != null) {
            for (int i = 0; i < bids.length; i++) {
                if (bids[i] != null) {
                    bids[i].clear();
                }
            }
        }
        if (asks != null) {
            for (int i = 0; i < asks.length; i++) {
                if (asks[i] != null) {
                    asks[i].clear();
                }
            }
        }
    }
}