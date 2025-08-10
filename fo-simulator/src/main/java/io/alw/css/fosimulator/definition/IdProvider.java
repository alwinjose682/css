package io.alw.css.fosimulator.definition;

import java.util.concurrent.atomic.AtomicLong;

final class IdProvider {
    private static IdProvider instance;
    private static final long defaultInitialTradeId = 1000000L;
    private static final long defaultInitialFoCfId = 10000L;

    private final AtomicLong tradeIdGenerator;
    private final AtomicLong foCfIdGenerator;

    private IdProvider() {
        this.tradeIdGenerator = new AtomicLong(defaultInitialTradeId);
        this.foCfIdGenerator = new AtomicLong(defaultInitialFoCfId);
    }

    static IdProvider singleton() {
        if (instance == null) {
            synchronized (IdProvider.class) {
                if (instance == null) {
                    instance = new IdProvider();
                }
            }
        }
        return instance;
    }

    long nextTradeId() {
        return tradeIdGenerator.getAndIncrement();
    }

    long nextCashflowId() {
        return foCfIdGenerator.getAndIncrement();
    }
}
