package io.alw.css.tradepublisher.generator;

import io.alw.css.tradepublisher.trade.model.GeneratorDetail;
import io.alw.css.tradepublisher.trade.tradegenerator.GeneratorHandlerOutcomeDtoBuilder;

import java.util.ArrayList;
import java.util.List;

public sealed interface GeneratorHandlerOutcome {
    String msg();

    record Success(String msg,
                   List<GeneratorDetail> startedGenerators) implements GeneratorHandlerOutcome {
    }

    record Failure(String msg,
                   List<String> stoppedGenerators, // Those that were successfully started, but interrupted later due to failure of a trade generator
                   List<String> failedGenerators) implements GeneratorHandlerOutcome {
    }

    record ConcurrentOperation(String msg) implements GeneratorHandlerOutcome {
    }

    record GenericMessage(String msg) implements GeneratorHandlerOutcome {
    }

    static GeneratorHandlerOutcomeDto toDto(GeneratorHandlerOutcome outcome) {
        return switch (outcome) {
            case ConcurrentOperation concurrentOperation -> GeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(concurrentOperation.msg()))
                    .build();
            case Failure failure -> GeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(failure.msg()))
                    .stoppedGenerators(failure.stoppedGenerators())
                    .failedGenerators(failure.failedGenerators())
                    .build();
            case GenericMessage genericMessage -> GeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(genericMessage.msg()))
                    .build();
            case Success success -> GeneratorHandlerOutcomeDtoBuilder
                    .builder()
                    .msgs(List.of(success.msg()))
                    .startedGenerators(success.startedGenerators())
                    .build();
        };
    }

    static GeneratorHandlerOutcomeDto toDto(List<GeneratorHandlerOutcome> outcome) {
        List<GeneratorDetail> startedGenerators = new ArrayList<>();
        List<String> stoppedGenerators = new ArrayList<>();
        List<String> failedGenerators = new ArrayList<>();
        List<String> msgs = new ArrayList<>();

        outcome.stream()
                .map(GeneratorHandlerOutcome::toDto)
                .forEach(e -> {
                    startedGenerators.addAll(e.startedGenerators());
                    stoppedGenerators.addAll(e.stoppedGenerators());
                    failedGenerators.addAll(e.failedGenerators());
                    msgs.addAll(e.msgs());
                });

        return GeneratorHandlerOutcomeDtoBuilder.builder()
                .startedGenerators(startedGenerators)
                .stoppedGenerators(stoppedGenerators)
                .failedGenerators(failedGenerators)
                .msgs(msgs)
                .build();
    }
}
