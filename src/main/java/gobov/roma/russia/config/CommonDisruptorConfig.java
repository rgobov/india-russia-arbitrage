package gobov.roma.config;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import gobov.roma.disruptor.BaseQuoteEvent;
import gobov.roma.disruptor.BaseQuoteEventFactory;
import gobov.roma.disruptor.RussiaQuoteHandler; // Обработчик для russia
import gobov.roma.disruptor.ShoonyaMappingHandler; // Цепочка для shoonya
import gobov.roma.disruptor.ShoonyaSaveHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class CommonDisruptorConfig {

    @Value("${disruptor.buffer-size:1024}")
    private int ringBufferSize;

    @Value("${disruptor.wait-strategy:yielding}")
    private String waitStrategy;

    @Value("${disruptor.producer-type:multi}")
    private String producerType;

    @Bean
    public BaseQuoteEventFactory baseEventFactory() {
        return new BaseQuoteEventFactory();
    }

    @Bean(destroyMethod = "shutdown")
    public Disruptor<BaseQuoteEvent> commonDisruptor(
            BaseQuoteEventFactory factory,
            ShoonyaMappingHandler shoonyaMappingHandler,
            ShoonyaSaveHandler shoonyaSaveHandler,
            RussiaQuoteHandler russiaQuoteHandler) {

        WaitStrategy selectedStrategy;
        switch (waitStrategy.toLowerCase()) {
            case "blocking": selectedStrategy = new BlockingWaitStrategy(); break;
            case "busy-spin": selectedStrategy = new BusySpinWaitStrategy(); break;
            default: selectedStrategy = new YieldingWaitStrategy();
        }

        ProducerType type = "single".equals(producerType) ? ProducerType.SINGLE : ProducerType.MULTI;

        Disruptor<BaseQuoteEvent> disruptor = new Disruptor<>(
                factory,
                ringBufferSize,
                Executors.newSingleThreadExecutor(), // Или виртуальный пул, если нужно
                type,
                selectedStrategy
        );

        // Цепочка ответственности: shoonyaMapping -> shoonyaSave -> russiaHandler
        // Каждый обработчик проверяет тип и пропускает, если не его
        disruptor.handleEventsWith(shoonyaMappingHandler)
                .then(shoonyaSaveHandler)
                .then(russiaQuoteHandler);

        disruptor.start();
        return disruptor;
    }
}