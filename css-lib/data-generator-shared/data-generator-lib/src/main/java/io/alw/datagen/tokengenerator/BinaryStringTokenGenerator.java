package io.alw.datagen.tokengenerator;

import java.util.ArrayList;
import java.util.List;

public final class BinaryStringTokenGenerator<T1, T2> implements TokenGenerator<List<String>> {
    private final TokenGenerator<T1> tokenGenerator1;
    private final TokenGenerator<T2> tokenGenerator2;

    public BinaryStringTokenGenerator(TokenGenerator<T1> tokenGenerator1, TokenGenerator<T2> tokenGenerator2) {
        this.tokenGenerator1 = tokenGenerator1;
        this.tokenGenerator2 = tokenGenerator2;
    }

    public TokenGenerator<T1> tokenProvider1() {
        return tokenGenerator1;
    }

    public TokenGenerator<T2> tokenProvider2() {
        return tokenGenerator2;
    }

    @Override
    public List<String> next() {
        List<String> idValues = new ArrayList<>();
        idValues.add(tokenGenerator1.nextAsString());
        idValues.add(tokenGenerator2.nextAsString());
        return idValues;
    }

    @Override
    public List<String> current() {
        List<String> idValues = new ArrayList<>();
        idValues.add(tokenGenerator1.currentAsString());
        idValues.add(tokenGenerator2.currentAsString());
        return idValues;
    }

    @Override
    public String nextAsString() {
        return String.join("", next());
    }

    @Override
    public String currentAsString() {
        return String.join("", current());
    }
}
