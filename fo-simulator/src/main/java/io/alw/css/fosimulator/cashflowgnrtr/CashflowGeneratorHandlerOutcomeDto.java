package io.alw.css.fosimulator.cashflowgnrtr;

import io.alw.css.fosimulator.model.GeneratorDetail;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record CashflowGeneratorHandlerOutcomeDto(
        List<String> msgs,
        List<GeneratorDetail> startedGenerators,
        List<String> stoppedGenerators,
        List<String> failedGenerators
) {
}
