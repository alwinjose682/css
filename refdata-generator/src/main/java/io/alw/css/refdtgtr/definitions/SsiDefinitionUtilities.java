package io.alw.css.refdtgtr.definitions;

import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.definition.CountAware;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.datagen.formattingtemplate.WidthAwareConcatenatingTemplate;
import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.css.refdtgtr.provider.RefDataProvider;
import io.alw.datagen.tokengenerator.AlphaNumericTokenGenerator;
import io.alw.datagen.tokengenerator.LongTokenGenerator;

final class SsiDefinitionUtilities implements CountAware {
    private static SsiDefinitionUtilities utilities;

    private long counter;
    final AlphaNumericTokenGenerator idGenerator;
    final RefDataProvider refDataProvider;
    final StringRefDataProvider stringRefDataProvider;
    public final LongTokenGenerator bankAccountNumberGenerator;
    public final TokenFormattingTemplate<String, String> bicCodeTemplate;
    public final TokenFormattingTemplate<String, String> bankAccountNumberTemplate;

    private SsiDefinitionUtilities() {
        this.counter = 0L;
        this.idGenerator = new AlphaNumericTokenGenerator(5, new char[]{'s', '0', '0', '0'});
        this.refDataProvider = new RefDataProvider();
        this.stringRefDataProvider = new StringRefDataProvider();
        this.bankAccountNumberGenerator = new LongTokenGenerator(325485652L);
        this.bicCodeTemplate = new WidthAwareConcatenatingTemplate(ConfigParams.BIC_CODE_LENGTH);
        this.bankAccountNumberTemplate = new WidthAwareConcatenatingTemplate(15);
    }

    static SsiDefinitionUtilities singleton() {
        if (utilities == null) {
            synchronized (SsiDefinitionUtilities.class) {
                if (utilities == null) {
                    utilities = new SsiDefinitionUtilities();
                }
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
