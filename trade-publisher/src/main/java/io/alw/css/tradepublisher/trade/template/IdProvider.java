package io.alw.css.tradepublisher.trade.template;

import java.util.concurrent.atomic.AtomicLong;

public final class IdProvider {
    private static IdProvider instance;
    public static final long defaultInitialTradeId = 1054321L;

    private final AtomicLong tradeIdGenerator;

    private IdProvider(long initialTradeId) {
        this.tradeIdGenerator = new AtomicLong(initialTradeId);
    }

    public static void init(long initialTradeId) {
        if (instance == null) {
            synchronized (IdProvider.class) {
                if (instance == null) {
                    instance = new IdProvider(initialTradeId);
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
        return new IdProvider(defaultInitialTradeId);
    }

    long nextTradeId() {
        return tradeIdGenerator.getAndIncrement();
    }
}
