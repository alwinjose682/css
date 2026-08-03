package io.alw.css.tradepublisher;

import java.util.concurrent.atomic.AtomicLong;

public final class IdProvider {
    private static IdProvider instance;
    public static final long defaultInitialTradeId = 1054321L;
    public static final long defaultConfMatchEventId = 58255L;

    private final AtomicLong tradeIdGenerator;
    private final AtomicLong confMatchEventIdGenerator;

    private IdProvider(long initialTradeId, long matchEventId) {
        this.tradeIdGenerator = new AtomicLong(initialTradeId);
        this.confMatchEventIdGenerator = new AtomicLong(matchEventId);
    }

    public static void init(long initialTradeId, long initialMatchEventId) {
        if (instance == null) {
            synchronized (IdProvider.class) {
                if (instance == null) {
                    instance = new IdProvider(initialTradeId, initialMatchEventId);
                }
            }
        }
    }

    public static IdProvider singleton() {
        if (instance == null) {
            synchronized (IdProvider.class) {
                if (instance == null) {
                    instance = newIdProviderWithDefaultValues();
                }
            }
        }
        return instance;
    }

    private static IdProvider newIdProviderWithDefaultValues() {
        return new IdProvider(defaultInitialTradeId, defaultConfMatchEventId);
    }

    public long nextTradeId() {
        return tradeIdGenerator.getAndIncrement();
    }

    public long nextConfMatchEventId() {
        return confMatchEventIdGenerator.getAndIncrement();
    }
}
