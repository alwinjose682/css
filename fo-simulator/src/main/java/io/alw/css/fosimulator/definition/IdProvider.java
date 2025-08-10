package io.alw.css.fosimulator.definition;

import java.util.concurrent.atomic.AtomicLong;

public final class IdProvider {
    private static IdProvider instance;
    private static final long defaultInitialTradeId = 1000000L;
    private static final long defaultInitialFoCfId = 10000L;

    private final AtomicLong tradeIdGenerator;
    private final AtomicLong foCfIdGenerator;

    private IdProvider(long initialTradeId, long initialFoCfId) {
        this.tradeIdGenerator = new AtomicLong(initialTradeId);
        this.foCfIdGenerator = new AtomicLong(initialFoCfId);
    }

    public static void init(long initialTradeId, long initialFoCfId) {
        if (instance == null) {
            synchronized (IdProvider.class) {
                if (instance == null) {
                    instance = new IdProvider(initialTradeId, initialFoCfId);
                }
            }
        }
    }

    static IdProvider singleton() {
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
        return new IdProvider(defaultInitialTradeId, defaultInitialFoCfId);
    }

    long nextTradeId() {
        return tradeIdGenerator.getAndIncrement();
    }

    long nextCashflowId() {
        return foCfIdGenerator.getAndIncrement();
    }
}
