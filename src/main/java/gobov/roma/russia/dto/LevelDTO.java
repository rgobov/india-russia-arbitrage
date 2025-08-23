package gobov.roma.russia.dto;

import java.math.BigDecimal;

public final class LevelDTO {
    public BigDecimal price;
    public long volume;

    public LevelDTO() {}

    public LevelDTO(BigDecimal price, long volume) {
        this.price = price;
        this.volume = volume;
    }

    public void set(BigDecimal price, long volume) {
        this.price = price;
        this.volume = volume;
    }

    public void clear() {
        price = null;
        volume = 0L;
    }
}