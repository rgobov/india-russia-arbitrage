package gobov.roma.russia.disruptor;

import java.math.BigDecimal;

public final class QuoteLevel {
    public BigDecimal price;
    public long volume;

    public void set(BigDecimal price, long volume) {
        this.price = price;
        this.volume = volume;
    }

    public void clear() {
        price = null;
        volume = 0L;
    }
}