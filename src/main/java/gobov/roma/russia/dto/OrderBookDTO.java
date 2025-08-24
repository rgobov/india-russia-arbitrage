package gobov.roma.russia.dto;

import java.time.Instant;

public final class OrderBookDTO {
    public String symbol;
    public String exchange;
    public Instant timestamp;
    public LevelDTO[] bids;
    public LevelDTO[] asks;
    public int bidCount;
    public int askCount;

    public OrderBookDTO() {}

    public OrderBookDTO(int maxLevels) {
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
        timestamp = null;
        bidCount = 0;
        askCount = 0;
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
    public void updateFromEvent(String symbol, String exchange, Instant timestamp,
                                int bidCount, LevelDTO[] eventBids,
                                int askCount, LevelDTO[] eventAsks) {
        this.symbol = symbol;
        this.exchange = exchange;
        this.timestamp = timestamp;
        this.bidCount = bidCount;
        this.askCount = askCount;

        // Копируем только необходимое количество уровней
        for (int i = 0; i < bidCount; i++) {
            this.bids[i].set(eventBids[i].price, eventBids[i].volume);
        }
        for (int i = 0; i < askCount; i++) {
            this.asks[i].set(eventAsks[i].price, eventAsks[i].volume);
        }
    }
}
