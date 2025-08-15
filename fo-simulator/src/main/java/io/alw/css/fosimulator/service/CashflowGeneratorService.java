package io.alw.css.fosimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.alw.css.fosimulator.cashflowgnrtr.CashflowGeneratorHandler;
import io.alw.css.fosimulator.cashflowgnrtr.CashflowGeneratorHandlerOutcome;
import io.alw.css.fosimulator.cashflowgnrtr.CashflowGeneratorHandlerOutcomeDto;
import io.alw.css.fosimulator.model.CashflowGeneratorInitialValues;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashflowGeneratorService {
    private final Logger log = LoggerFactory.getLogger(CashflowGeneratorService.class);
    private final CashflowGeneratorHandler cashflowGeneratorHandler;
    private final ObjectMapper objectMapper;

    public CashflowGeneratorService(CashflowGeneratorHandler cashflowGeneratorHandler, ObjectMapper objectMapper) {
        this.cashflowGeneratorHandler = cashflowGeneratorHandler;
        this.objectMapper = objectMapper;
    }

    public CashflowGeneratorHandlerOutcomeDto start(@Valid CashflowGeneratorInitialValues initialGeneratorValues) throws JsonProcessingException {
        final CashflowGeneratorHandlerOutcome outcome;
        if (initialGeneratorValues != null) {
            outcome = cashflowGeneratorHandler.startAllGenerators(initialGeneratorValues);
        } else {
            outcome = cashflowGeneratorHandler.startAllGenerators();
        }

        CashflowGeneratorHandlerOutcomeDto outcomeDto = CashflowGeneratorHandlerOutcome.toDto(outcome);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcomeDto));
        return outcomeDto;
    }

    public CashflowGeneratorHandlerOutcomeDto stop() throws JsonProcessingException {
        List<CashflowGeneratorHandlerOutcome> outcomesList = cashflowGeneratorHandler.stopAllGenerators();
        CashflowGeneratorHandlerOutcomeDto outcome = CashflowGeneratorHandlerOutcome.toDto(outcomesList);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcome));
        return outcome;
    }

    public CashflowGeneratorHandlerOutcomeDto start(String generatorKey, CashflowGeneratorInitialValues initialGeneratorValues) {
        CashflowGeneratorHandlerOutcome.Failure outcome = new CashflowGeneratorHandlerOutcome.Failure("Adhoc generator starting is not fully implemented yet", null, null);
        return CashflowGeneratorHandlerOutcome.toDto(outcome);
    }

    public CashflowGeneratorHandlerOutcomeDto stop(String generatorKey) {
        CashflowGeneratorHandlerOutcome.Failure outcome = new CashflowGeneratorHandlerOutcome.Failure("Adhoc generator stopping is not fully implemented yet", null, null);
        return CashflowGeneratorHandlerOutcome.toDto(outcome);
    }
}
