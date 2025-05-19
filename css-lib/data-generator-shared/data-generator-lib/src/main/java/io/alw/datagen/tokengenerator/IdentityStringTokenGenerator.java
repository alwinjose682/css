package io.alw.datagen.tokengenerator;

public final class IdentityStringTokenGenerator implements TokenGenerator<String> {
    private final String constToken;

    public IdentityStringTokenGenerator(String constToken) {
        this.constToken = constToken;
    }

    @Override
    public String next() {
        return constToken;
    }

    @Override
    public String current() {
        return constToken;
    }

    @Override
    public String nextAsString() {
        return constToken;
    }

    @Override
    public String currentAsString() {
        return constToken;
    }
}
