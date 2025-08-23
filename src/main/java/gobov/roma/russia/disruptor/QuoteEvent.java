package gobov.roma.russia.disruptor;

import gobov.roma.russia.dto.LevelDTO;

public final class QuoteEvent {
    public String symbol;
    public String exchange;
    public boolean snapshot;
    public LevelDTO[] bids;
    public LevelDTO[] asks;
    public int bidCount;
    public int askCount;
    public long timestamp;
    public long msTimestamp;
    public boolean existing;
    public String guid;

    public QuoteEvent(int maxLevels) {
        bids = new LevelDTO[maxLevels];
        asks = new LevelDTO[maxLevels];
        for (int i = 0; i < maxLevels; i++) {
            bids[i] = new LevelDTO();
            asks[i] = new LevelDTO();
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