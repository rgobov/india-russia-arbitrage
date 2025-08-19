package gobov.roma.russia.disruptor;

import com.lmax.disruptor.EventFactory;

public class QuoteEventFactory implements EventFactory<QuoteEvent> {
    @Override
    public QuoteEvent newInstance() {
        return new QuoteEvent();
    }
}
