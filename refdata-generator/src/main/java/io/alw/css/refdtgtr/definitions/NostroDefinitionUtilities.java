package io.alw.css.refdtgtr.definitions;

import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.definition.CountAware;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.datagen.formattingtemplate.WidthAwareConcatenatingTemplate;
import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.AlphaNumericTokenGenerator;
import io.alw.datagen.tokengenerator.LongTokenGenerator;

import java.util.random.RandomGenerator;

public final class NostroDefinitionUtilities implements CountAware {
    private static NostroDefinitionUtilities utilities;

    private long counter;
    public final AlphaNumericTokenGenerator idGenerator;
    public final AlphaNumericTokenGenerator slaCodeGenerator;
    public final LongTokenGenerator bankAccountNumberGenerator;
    public final TokenFormattingTemplate<String, String> bicCodeTemplate;
    public final TokenFormattingTemplate<String, String> bankAccountNumberTemplate;
    public final RefDataProvider refDataProvider;
    public final StringRefDataProvider stringRefDataProvider;
    public final StringRefDataProvider counterpartyTypeProvider;
    public final RandomGenerator rndmGntr;

    private NostroDefinitionUtilities() {
        this.counter = 0L;

        this.idGenerator = new AlphaNumericTokenGenerator(4, new char[]{'n', '6', 'a'});
        this.slaCodeGenerator = new AlphaNumericTokenGenerator(5, new char[]{'s', 'l', '1', '1'});
        this.bankAccountNumberGenerator = new LongTokenGenerator(15485254L);
        this.bicCodeTemplate = new WidthAwareConcatenatingTemplate(ConfigParams.BIC_CODE_LENGTH);
        this.bankAccountNumberTemplate = new WidthAwareConcatenatingTemplate(15);
        this.refDataProvider = new RefDataProvider();
        this.stringRefDataProvider = new StringRefDataProvider();
        this.counterpartyTypeProvider = new StringRefDataProvider();
        this.rndmGntr = RandomGenerator.getDefault();
    }

    public static NostroDefinitionUtilities singleton() {
        if (utilities == null) {
            synchronized (NostroDefinitionUtilities.class) {
                if (utilities == null) {
                    utilities = new NostroDefinitionUtilities();
                }
                return utilities;
            }
        }
        utilities.incrementCounter();
        return utilities;
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

