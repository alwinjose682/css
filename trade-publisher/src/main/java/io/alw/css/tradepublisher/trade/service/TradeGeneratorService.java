package io.alw.css.tradepublisher.trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.alw.css.tradepublisher.trade.model.TradeGenerationInitialValues;
import io.alw.css.tradepublisher.trade.tradegenerator.TradeGeneratorHandler;
import io.alw.css.tradepublisher.trade.tradegenerator.TradeGeneratorHandlerOutcome;
import io.alw.css.tradepublisher.trade.tradegenerator.TradeGeneratorHandlerOutcomeDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeGeneratorService {
    private final Logger log = LoggerFactory.getLogger(TradeGeneratorService.class);
    private final TradeGeneratorHandler tradeGeneratorHandler;
    private final ObjectMapper objectMapper;

    public TradeGeneratorService(TradeGeneratorHandler tradeGeneratorHandler, ObjectMapper objectMapper) {
        this.tradeGeneratorHandler = tradeGeneratorHandler;
        this.objectMapper = objectMapper;
    }

    public TradeGeneratorHandlerOutcomeDto start(@Valid TradeGenerationInitialValues cfGenerationInitialValues) throws JsonProcessingException {
        final TradeGeneratorHandlerOutcome outcome;
        if (cfGenerationInitialValues != null) {
            outcome = tradeGeneratorHandler.startAllGenerators(cfGenerationInitialValues);
        } else {
            outcome = tradeGeneratorHandler.startAllGenerators();
        }

        TradeGeneratorHandlerOutcomeDto outcomeDto = TradeGeneratorHandlerOutcome.toDto(outcome);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcomeDto));
        return outcomeDto;
    }

    public TradeGeneratorHandlerOutcomeDto stop() throws JsonProcessingException {
        List<TradeGeneratorHandlerOutcome> outcomesList = tradeGeneratorHandler.stopAllGenerators();
        TradeGeneratorHandlerOutcomeDto outcome = TradeGeneratorHandlerOutcome.toDto(outcomesList);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcome));
        return outcome;
    }

    public TradeGeneratorHandlerOutcomeDto start(String generatorKey, TradeGenerationInitialValues initialGeneratorValues) {
        TradeGeneratorHandlerOutcome.Failure outcome = new TradeGeneratorHandlerOutcome.Failure("Adhoc generator starting is not fully implemented yet", null, null);
        return TradeGeneratorHandlerOutcome.toDto(outcome);
    }

    public TradeGeneratorHandlerOutcomeDto stop(String generatorKey) {
        TradeGeneratorHandlerOutcome.Failure outcome = new TradeGeneratorHandlerOutcome.Failure("Adhoc generator stopping is not fully implemented yet", null, null);
        return TradeGeneratorHandlerOutcome.toDto(outcome);
    }
}
