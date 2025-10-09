package io.alw.css.fosimulator.cashflowgnrtr;

import io.alw.css.fosimulator.model.GeneratorDetail;

import java.util.ArrayList;
import java.util.List;

public sealed interface CashflowGeneratorHandlerOutcome {
    String msg();

    record Success(String msg,
                   List<GeneratorDetail> startedGenerators) implements CashflowGeneratorHandlerOutcome {
    }

    record Failure(String msg,
                   List<String> stoppedGenerators, // Those that were successfully started, but interrupted later due to failure of a cashflow generator
                   List<String> failedGenerators) implements CashflowGeneratorHandlerOutcome {
    }

    record ConcurrentOperation(String msg) implements CashflowGeneratorHandlerOutcome {
    }

    record GenericMessage(String msg) implements CashflowGeneratorHandlerOutcome {
    }

    static CashflowGeneratorHandlerOutcomeDto toDto(CashflowGeneratorHandlerOutcome outcome) {
        return switch (outcome) {
            case ConcurrentOperation concurrentOperation -> CashflowGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(concurrentOperation.msg()))
                    .build();
            case Failure failure -> CashflowGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(failure.msg()))
                    .stoppedGenerators(failure.stoppedGenerators())
                    .failedGenerators(failure.failedGenerators())
                    .build();
            case GenericMessage genericMessage -> CashflowGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(genericMessage.msg()))
                    .build();
            case Success success -> CashflowGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(success.msg()))
                    .startedGenerators(success.startedGenerators())
                    .build();
        };
    }

    static CashflowGeneratorHandlerOutcomeDto toDto(List<CashflowGeneratorHandlerOutcome> outcome) {
        List<GeneratorDetail> startedGenerators = new ArrayList<>();
        List<String> stoppedGenerators = new ArrayList<>();
        List<String> failedGenerators = new ArrayList<>();
        List<String> msgs = new ArrayList<>();

        outcome.stream()
                .map(CashflowGeneratorHandlerOutcome::toDto)
                .forEach(e -> {
                    startedGenerators.addAll(e.startedGenerators());
                    stoppedGenerators.addAll(e.stoppedGenerators());
                    failedGenerators.addAll(e.failedGenerators());
                    msgs.addAll(e.msgs());
                });

        return CashflowGeneratorHandlerOutcomeDtoBuilder.builder()
                .startedGenerators(startedGenerators)
                .stoppedGenerators(stoppedGenerators)
                .failedGenerators(failedGenerators)
                .msgs(msgs)
                .build();
    }
}
