package gobov.roma.disruptor;

import lombok.Data;

@Data
public abstract class BaseQuoteEvent {
    private EventType type; // Enum для типа: SHOONYA или RUSSIA

    public enum EventType {
        SHOONYA,
        RUSSIA
    }

    public abstract void clear();
}