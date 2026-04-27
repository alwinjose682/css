package io.alw.css.refdtgtr.templates;

import io.alw.css.refdtgtr.config.ConfigParams;
import io.alw.datagen.template.CountAware;
import io.alw.datagen.formattingtemplate.TokenFormattingTemplate;
import io.alw.datagen.formattingtemplate.WidthAwareConcatenatingTemplate;
import io.alw.css.refdtgtr.provider.StringRefDataProvider;
import io.alw.datagen.tokengenerator.AlphaNumericTokenGenerator;
import io.alw.datagen.tokengenerator.LongTokenGenerator;

final class SsiTemplateUtilities implements CountAware {
    private static SsiTemplateUtilities utilities;

    private long counter;
    final AlphaNumericTokenGenerator idGenerator;
    final StringRefDataProvider stringRefDataProvider;
    public final LongTokenGenerator bankAccountNumberGenerator;
    public final TokenFormattingTemplate<String, String> bicCodeTemplate;
    public final TokenFormattingTemplate<String, String> bankAccountNumberTemplate;

    private SsiTemplateUtilities() {
        this.counter = 0L;
        this.idGenerator = new AlphaNumericTokenGenerator(5, new char[]{'s', '0', '0', '0'});
        this.stringRefDataProvider = new StringRefDataProvider();
        this.bankAccountNumberGenerator = new LongTokenGenerator(325485652L);
        this.bicCodeTemplate = new WidthAwareConcatenatingTemplate(ConfigParams.BIC_CODE_LENGTH);
        this.bankAccountNumberTemplate = new WidthAwareConcatenatingTemplate(15);
    }

    static SsiTemplateUtilities singleton() {
        if (utilities == null) {
            synchronized (SsiTemplateUtilities.class) {
                if (utilities == null) {
                    utilities = new SsiTemplateUtilities();
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
