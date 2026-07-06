package io.alw.css.tradepublisher.trade.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.alw.css.tradepublisher.trade.model.TradeGenerationInitialValues;
import io.alw.css.tradepublisher.trade.service.TradeGeneratorService;
import io.alw.css.tradepublisher.trade.tradegenerator.TradeGeneratorHandlerOutcomeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = TradeGeneratorController.CF_GEN_URL)
public class TradeGeneratorController {
    private static final Logger log = LoggerFactory.getLogger(TradeGeneratorController.class);
    static final String CF_GEN_URL = "/trade/generators";
    static final String ALL_GENERATORS_KEY = "all";

    private final TradeGeneratorService tradeGeneratorService;

    public TradeGeneratorController(TradeGeneratorService tradeGeneratorService) {
        this.tradeGeneratorService = tradeGeneratorService;
    }

    @PutMapping(value = "start/{generatorKey}",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<TradeGeneratorHandlerOutcomeDto> start(@PathVariable String generatorKey,
                                                                 @RequestBody TradeGenerationInitialValues initialGeneratorValues) throws JsonProcessingException {
        log.debug("Received Trade-Generator start request with params- generatorKey: {}, initialGeneratorValues: {}", generatorKey, initialGeneratorValues);

        final TradeGeneratorHandlerOutcomeDto outcome;
        if (generatorKey.equalsIgnoreCase(ALL_GENERATORS_KEY)) {
            outcome = tradeGeneratorService.start(initialGeneratorValues);
        } else {
            outcome = tradeGeneratorService.start(generatorKey, initialGeneratorValues);
        }

        return new ResponseEntity<>(outcome, HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "stop/{generatorKey}",
            produces = "application/json")
    public ResponseEntity<TradeGeneratorHandlerOutcomeDto> stop(@PathVariable String generatorKey) throws JsonProcessingException {
        final TradeGeneratorHandlerOutcomeDto outcome;
        if (generatorKey.equalsIgnoreCase(ALL_GENERATORS_KEY)) {
            outcome = tradeGeneratorService.stop();
        } else {
            outcome = tradeGeneratorService.stop(generatorKey);
        }

        return new ResponseEntity<>(outcome, HttpStatus.ACCEPTED);
    }
}
