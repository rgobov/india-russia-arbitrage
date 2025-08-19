package gobov.roma.russia.config;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import gobov.roma.russia.disruptor.QuoteEvent;
import gobov.roma.russia.disruptor.QuoteEventFactory;
import gobov.roma.russia.disruptor.QuoteEventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

@Configuration
public class DisruptorConfig {

    @Value("${disruptor.buffer-size:1024}")
    private int ringBufferSize;

    @Value("${disruptor.wait-strategy:yielding}")
    private String waitStrategy;

    @Value("${disruptor.producer-type:multi}")
    private String producerType;

    @Bean
    public QuoteEventFactory eventFactory() {
        return new QuoteEventFactory();
    }


    @Bean(destroyMethod = "shutdown")
    public Disruptor<QuoteEvent> disruptor(
            QuoteEventFactory factory,
            QuoteEventHandler handler,
            ExecutorService virtualThreadExecutor) { // Внедряем общий бин

        WaitStrategy selectedStrategy;
        switch (waitStrategy.toLowerCase()) {
            case "blocking": selectedStrategy = new BlockingWaitStrategy(); break;
            case "busy-spin": selectedStrategy = new BusySpinWaitStrategy(); break;
            default: selectedStrategy = new YieldingWaitStrategy();
        }

        ProducerType type = "single".equals(producerType)
                ? ProducerType.SINGLE
                : ProducerType.MULTI;

        Disruptor<QuoteEvent> disruptor = new Disruptor<>(
                factory,
                ringBufferSize,
                virtualThreadExecutor, // Используем общий пул
                type,
                selectedStrategy
        );

        disruptor.handleEventsWith(handler);
        disruptor.start();
        return disruptor;
    }
}