package gobov.roma.disruptor;

import com.lmax.disruptor.EventHandler;
import gobov.roma.india.shoonya.entity.QuoteEntity;
import gobov.roma.india.shoonya.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class ShoonyaSaveHandler implements EventHandler<BaseQuoteEvent> {

    private final QuoteRepository quoteRepository;
    private final ExecutorService virtualThreadExecutor;
    private static final Logger logger = LoggerFactory.getLogger(ShoonyaSaveHandler.class);

    @Autowired
    public ShoonyaSaveHandler(QuoteRepository quoteRepository, ExecutorService virtualThreadExecutor) {
        this.quoteRepository = quoteRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public void onEvent(BaseQuoteEvent event, long sequence, boolean endOfBatch) {
        if (!(event instanceof ShoonyaQuoteEvent shoonyaEvent)) {
            return; // Пропустить
        }

        List<QuoteEntity> entities = shoonyaEvent.getEntities();
        if (entities == null || entities.isEmpty()) return;

        virtualThreadExecutor.execute(() -> {
            saveTransactional(entities);
        });
    }

    @Transactional
    protected void saveTransactional(List<QuoteEntity> entities) {
        quoteRepository.saveAll(entities);
        logger.info("Сохранено {} котировок shoonya в виртуальном потоке", entities.size());
    }
}