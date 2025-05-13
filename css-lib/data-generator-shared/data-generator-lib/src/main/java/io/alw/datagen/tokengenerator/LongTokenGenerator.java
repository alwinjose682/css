package io.alw.datagen.tokengenerator;

public final class LongTokenGenerator implements TokenGenerator<Long> {
    private long token;

    public LongTokenGenerator() {
        this.token = 0;
    }

    public LongTokenGenerator(long initialValue) {
        this.token = initialValue;
    }

    @Override
    public Long next() {
        return ++token;
    }

    @Override
    public Long current() {
        return token;
    }
}
