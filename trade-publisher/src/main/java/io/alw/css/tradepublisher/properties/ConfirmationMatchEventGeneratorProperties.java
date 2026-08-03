package io.alw.css.tradepublisher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("confirmation.matchevent.generator")
public class ConfirmationMatchEventGeneratorProperties {
    private final long amendmentFrequencySeconds;

    @ConstructorBinding
    public ConfirmationMatchEventGeneratorProperties(long amendmentFrequencySeconds) {
        this.amendmentFrequencySeconds = amendmentFrequencySeconds;
    }

    public long amendmentFrequencySeconds() {
        return amendmentFrequencySeconds;
    }
}
