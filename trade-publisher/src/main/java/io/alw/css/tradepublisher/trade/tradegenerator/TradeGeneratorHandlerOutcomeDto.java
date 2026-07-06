package io.alw.css.tradepublisher.trade.tradegenerator;

import io.alw.css.tradepublisher.trade.model.GeneratorDetail;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record TradeGeneratorHandlerOutcomeDto(
        List<String> msgs,
        List<GeneratorDetail> startedGenerators,
        List<String> stoppedGenerators,
        List<String> failedGenerators
) {
}
