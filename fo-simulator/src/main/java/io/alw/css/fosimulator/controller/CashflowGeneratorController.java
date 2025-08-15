package io.alw.css.fosimulator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.alw.css.fosimulator.cashflowgnrtr.CashflowGeneratorHandlerOutcome;
import io.alw.css.fosimulator.cashflowgnrtr.CashflowGeneratorHandlerOutcomeDto;
import io.alw.css.fosimulator.model.CashflowGeneratorInitialValues;
import io.alw.css.fosimulator.service.CashflowGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = CashflowGeneratorController.CF_GEN_URL)
public class CashflowGeneratorController {
    static final String CF_GEN_URL = "/cashflow/generators";
    static final String ALL_GENERATORS_KEY = "all";

    private final CashflowGeneratorService cashflowGeneratorService;

    public CashflowGeneratorController(CashflowGeneratorService cashflowGeneratorService) {
        this.cashflowGeneratorService = cashflowGeneratorService;
    }

    @PutMapping(value = "start/{generatorKey}",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<CashflowGeneratorHandlerOutcomeDto> start(@PathVariable String generatorKey,
                                                                    @RequestBody CashflowGeneratorInitialValues initialGeneratorValues) throws JsonProcessingException {
        final CashflowGeneratorHandlerOutcomeDto outcome;
        if (generatorKey.equalsIgnoreCase(ALL_GENERATORS_KEY)) {
            outcome = cashflowGeneratorService.start(initialGeneratorValues);
        } else {
            outcome = cashflowGeneratorService.start(generatorKey, initialGeneratorValues);
        }

        return new ResponseEntity<>(outcome, HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "stop/{generatorKey}",
            produces = "application/json")
    public ResponseEntity<CashflowGeneratorHandlerOutcomeDto> stop(@PathVariable String generatorKey) throws JsonProcessingException {
        final CashflowGeneratorHandlerOutcomeDto outcome;
        if (generatorKey.equalsIgnoreCase(ALL_GENERATORS_KEY)) {
            outcome = cashflowGeneratorService.stop();
        } else {
            outcome = cashflowGeneratorService.stop(generatorKey);
        }

        return new ResponseEntity<>(outcome, HttpStatus.ACCEPTED);
    }
}
