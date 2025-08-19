package gobov.roma.russia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public ScheduledExecutorService scheduledVirtualThreadExecutor() {
        return Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
    }
//
//    @Bean
//    public ExecutorService threadPoolExecutor() {
//        return Executors.newFixedThreadPool(10);
//    }
}