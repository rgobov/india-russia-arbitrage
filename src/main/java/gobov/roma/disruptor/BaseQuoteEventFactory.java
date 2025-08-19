package gobov.roma.disruptor;

import com.lmax.disruptor.EventFactory;

public class BaseQuoteEventFactory implements EventFactory<BaseQuoteEvent> {
    @Override
    public BaseQuoteEvent newInstance() {
        return new BaseQuoteEvent() {
            @Override
            public void clear() {

            }
        };
    }
}
