package gobov.roma.russia.disruptor;

import com.lmax.disruptor.EventFactory;

public class QuoteEventFactory implements EventFactory<QuoteEvent> {
    private final int maxLevels;

    public QuoteEventFactory(int maxLevels) {
        this.maxLevels = maxLevels;
    }

    @Override
    public QuoteEvent newInstance() {
        return new QuoteEvent(maxLevels);
    }
}