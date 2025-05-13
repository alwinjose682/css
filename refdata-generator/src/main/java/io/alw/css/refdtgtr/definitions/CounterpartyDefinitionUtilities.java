package io.alw.css.refdtgtr.definitions;

import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.definition.CountAware;
import io.alw.datagen.formattingtemplate.SimpleConcatenatingTemplate;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.datagen.formattingtemplate.WidthAwareConcatenatingTemplate;
import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.BinaryStringTokenGenerator;
import io.alw.datagen.tokengenerator.IdentityStringTokenGenerator;
import io.alw.datagen.tokengenerator.LongTokenGenerator;

public final class CounterpartyDefinitionUtilities implements CountAware {
    private static CounterpartyDefinitionUtilities utilities;

    private long counter;
    private final BinaryStringTokenGenerator<String, Long> idProvider;
    private final TokenFormattingTemplate<String, String> simpleConcatenatingTemplate;
    private final TokenFormattingTemplate<String, String> bicCodeTemplate;
    private final RefDataProvider refDataProvider;
    private final StringRefDataProvider counterpartyTypeProvider;

    private CounterpartyDefinitionUtilities() {
        this.counter = 0L;
        this.idProvider = new BinaryStringTokenGenerator<>(new IdentityStringTokenGenerator("CP"), new LongTokenGenerator());
        this.simpleConcatenatingTemplate = SimpleConcatenatingTemplate.singleton();
        this.bicCodeTemplate = new WidthAwareConcatenatingTemplate(ConfigParams.BIC_CODE_LENGTH);
        this.refDataProvider = new RefDataProvider();
        this.counterpartyTypeProvider = new StringRefDataProvider();
    }

    public static CounterpartyDefinitionUtilities singleton() {
        if (utilities == null) {
            synchronized (CounterpartyDefinitionUtilities.class) {
                if (utilities == null) {
                    utilities = new CounterpartyDefinitionUtilities();
                }
                return utilities;
            }
        }
        utilities.incrementCounter();
        return utilities;
    }

    public TokenFormattingTemplate<String, String> simpleConcatenatingTemplate() {
        return simpleConcatenatingTemplate;
    }

    public StringRefDataProvider counterpartyTypeProvider() {
        return counterpartyTypeProvider;
    }

    public BinaryStringTokenGenerator<String, Long> idProvider() {
        return idProvider;
    }

    public RefDataProvider testDataProvider() {
        return refDataProvider;
    }

    public TokenFormattingTemplate<String, String> bicCodeTemplate() {
        return bicCodeTemplate;
    }

    @Override
    public long counter() {
        return counter;
    }

    @Override
    public void incrementCounter() {
        ++counter;
    }
}
