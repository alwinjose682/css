package io.alw.css.tradepublisher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("matchstatusevent.generator")
public class MatchStatusEventGeneratorProperties {
    private final long amendmentFrequencySeconds;

    @ConstructorBinding
    public MatchStatusEventGeneratorProperties(long amendmentFrequencySeconds) {
        this.amendmentFrequencySeconds = amendmentFrequencySeconds;
    }

    public long amendmentFrequencySeconds() {
        return amendmentFrequencySeconds;
    }
}
