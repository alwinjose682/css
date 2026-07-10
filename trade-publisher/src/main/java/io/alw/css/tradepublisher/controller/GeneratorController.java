package io.alw.css.tradepublisher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.alw.css.tradepublisher.generator.GeneratorHandlerOutcomeDto;
import io.alw.css.tradepublisher.trade.model.GeneratorInitialValues;
import io.alw.css.tradepublisher.trade.service.GeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = GeneratorController.CF_GEN_URL)
public class GeneratorController {
    private static final Logger log = LoggerFactory.getLogger(GeneratorController.class);
    static final String CF_GEN_URL = "/trade/generators";
    static final String ALL_GENERATORS_KEY = "all";

    private final GeneratorService generatorService;

    public GeneratorController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PutMapping(value = "start/{generatorKey}",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<GeneratorHandlerOutcomeDto> start(@PathVariable String generatorKey,
                                                            @RequestBody GeneratorInitialValues initialGeneratorValues) throws JsonProcessingException {
        log.debug("Received Generator(Trade and ConfirmationMatchStatus) start request with params- generatorKey: {}, initialGeneratorValues: {}", generatorKey, initialGeneratorValues);

        final GeneratorHandlerOutcomeDto outcome;
        if (generatorKey.equalsIgnoreCase(ALL_GENERATORS_KEY)) {
            outcome = generatorService.start(initialGeneratorValues);
        } else {
            outcome = generatorService.start(generatorKey, initialGeneratorValues);
        }

        return new ResponseEntity<>(outcome, HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "stop/{generatorKey}",
            produces = "application/json")
    public ResponseEntity<GeneratorHandlerOutcomeDto> stop(@PathVariable String generatorKey) throws JsonProcessingException {
        final GeneratorHandlerOutcomeDto outcome;
        if (generatorKey.equalsIgnoreCase(ALL_GENERATORS_KEY)) {
            outcome = generatorService.stop();
        } else {
            outcome = generatorService.stop(generatorKey);
        }

        return new ResponseEntity<>(outcome, HttpStatus.ACCEPTED);
    }
}
