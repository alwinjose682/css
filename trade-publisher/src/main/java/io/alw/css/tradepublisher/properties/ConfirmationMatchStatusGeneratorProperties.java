package io.alw.css.tradepublisher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("confirmation.matchstatus.generator")
public class ConfirmationMatchStatusGeneratorProperties {
    private final long amendmentFrequencySeconds;

    @ConstructorBinding
    public ConfirmationMatchStatusGeneratorProperties(long amendmentFrequencySeconds) {
        this.amendmentFrequencySeconds = amendmentFrequencySeconds;
    }

    public long amendmentFrequencySeconds() {
        return amendmentFrequencySeconds;
    }
}
