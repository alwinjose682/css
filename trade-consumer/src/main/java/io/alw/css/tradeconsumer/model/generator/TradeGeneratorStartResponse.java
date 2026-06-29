package io.alw.css.tradeconsumer.model.generator;

import java.util.List;

public record TradeGeneratorStartResponse(
        List<String> msgs,
        List<GeneratorDetail> startedGenerators,
        List<String> stoppedGenerators,
        List<String> failedGenerators
) {
}
