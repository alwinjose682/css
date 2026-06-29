package io.alw.css.tradepublisher.tradegenerator;

import io.alw.css.tradepublisher.model.GeneratorDetail;

import java.util.ArrayList;
import java.util.List;

public sealed interface TradeGeneratorHandlerOutcome {
    String msg();

    record Success(String msg,
                   List<GeneratorDetail> startedGenerators) implements TradeGeneratorHandlerOutcome {
    }

    record Failure(String msg,
                   List<String> stoppedGenerators, // Those that were successfully started, but interrupted later due to failure of a trade generator
                   List<String> failedGenerators) implements TradeGeneratorHandlerOutcome {
    }

    record ConcurrentOperation(String msg) implements TradeGeneratorHandlerOutcome {
    }

    record GenericMessage(String msg) implements TradeGeneratorHandlerOutcome {
    }

    static TradeGeneratorHandlerOutcomeDto toDto(TradeGeneratorHandlerOutcome outcome) {
        return switch (outcome) {
            case ConcurrentOperation concurrentOperation -> TradeGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(concurrentOperation.msg()))
                    .build();
            case Failure failure -> TradeGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(failure.msg()))
                    .stoppedGenerators(failure.stoppedGenerators())
                    .failedGenerators(failure.failedGenerators())
                    .build();
            case GenericMessage genericMessage -> TradeGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(genericMessage.msg()))
                    .build();
            case Success success -> TradeGeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(success.msg()))
                    .startedGenerators(success.startedGenerators())
                    .build();
        };
    }

    static TradeGeneratorHandlerOutcomeDto toDto(List<TradeGeneratorHandlerOutcome> outcome) {
        List<GeneratorDetail> startedGenerators = new ArrayList<>();
        List<String> stoppedGenerators = new ArrayList<>();
        List<String> failedGenerators = new ArrayList<>();
        List<String> msgs = new ArrayList<>();

        outcome.stream()
                .map(TradeGeneratorHandlerOutcome::toDto)
                .forEach(e -> {
                    startedGenerators.addAll(e.startedGenerators());
                    stoppedGenerators.addAll(e.stoppedGenerators());
                    failedGenerators.addAll(e.failedGenerators());
                    msgs.addAll(e.msgs());
                });

        return TradeGeneratorHandlerOutcomeDtoBuilder.builder()
                .startedGenerators(startedGenerators)
                .stoppedGenerators(stoppedGenerators)
                .failedGenerators(failedGenerators)
                .msgs(msgs)
                .build();
    }
}
