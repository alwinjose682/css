package io.alw.css.tradepublisher.trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.alw.css.tradepublisher.generator.GeneratorHandler;
import io.alw.css.tradepublisher.generator.GeneratorHandlerOutcome;
import io.alw.css.tradepublisher.generator.GeneratorHandlerOutcomeDto;
import io.alw.css.tradepublisher.trade.model.GeneratorInitialValues;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeneratorService {
    private final Logger log = LoggerFactory.getLogger(GeneratorService.class);
    private final GeneratorHandler generatorHandler;
    private final ObjectMapper objectMapper;

    public GeneratorService(GeneratorHandler generatorHandler, ObjectMapper objectMapper) {
        this.generatorHandler = generatorHandler;
        this.objectMapper = objectMapper;
    }

    public GeneratorHandlerOutcomeDto start(@Valid GeneratorInitialValues generatorInitialValues) throws JsonProcessingException {
        final GeneratorHandlerOutcome outcome;
        if (generatorInitialValues != null) {
            outcome = generatorHandler.startAllGenerators(generatorInitialValues);
        } else {
            outcome = generatorHandler.startAllGenerators();
        }

        GeneratorHandlerOutcomeDto outcomeDto = GeneratorHandlerOutcome.toDto(outcome);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcomeDto));
        return outcomeDto;
    }

    public GeneratorHandlerOutcomeDto stop() throws JsonProcessingException {
        List<GeneratorHandlerOutcome> outcomesList = generatorHandler.stopAllGenerators();
        GeneratorHandlerOutcomeDto outcome = GeneratorHandlerOutcome.toDto(outcomesList);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcome));
        return outcome;
    }

    public GeneratorHandlerOutcomeDto start(String generatorKey, GeneratorInitialValues initialGeneratorValues) {
        GeneratorHandlerOutcome.Failure outcome = new GeneratorHandlerOutcome.Failure("Adhoc generator starting is not fully implemented yet", null, null);
        return GeneratorHandlerOutcome.toDto(outcome);
    }

    public GeneratorHandlerOutcomeDto stop(String generatorKey) {
        GeneratorHandlerOutcome.Failure outcome = new GeneratorHandlerOutcome.Failure("Adhoc generator stopping is not fully implemented yet", null, null);
        return GeneratorHandlerOutcome.toDto(outcome);
    }
}
