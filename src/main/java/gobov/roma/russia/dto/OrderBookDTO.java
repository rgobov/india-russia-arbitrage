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

    public OrderBookDTO() {};

    public OrderBookDTO(int maxLevels) {
        bids = new LevelDTO[maxLevels];
        asks = new LevelDTO[maxLevels];
        for (int i = 0; i < maxLevels; i++) {
            bids[i] = new LevelDTO();
            asks[i] = new LevelDTO();
        }
    }
}