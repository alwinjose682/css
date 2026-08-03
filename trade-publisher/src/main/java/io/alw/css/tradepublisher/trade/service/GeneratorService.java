package io.alw.css.tradepublisher.trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.alw.css.tradepublisher.confirmation.KafkaConsumer;
import io.alw.css.tradepublisher.generator.GeneratorHandler;
import io.alw.css.tradepublisher.generator.GeneratorHandlerOutcome;
import io.alw.css.tradepublisher.generator.GeneratorHandlerOutcomeDto;
import io.alw.css.tradepublisher.trade.model.GeneratorInitialValues;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeneratorService {
    private final Logger log = LoggerFactory.getLogger(GeneratorService.class);
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final GeneratorHandler generatorHandler;
    private final KafkaConsumer kafkaConsumer;
    private final ObjectMapper objectMapper;

    public GeneratorService(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, KafkaConsumer kafkaConsumer, GeneratorHandler generatorHandler, ObjectMapper objectMapper) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.generatorHandler = generatorHandler;
        this.kafkaConsumer = kafkaConsumer;
        this.objectMapper = objectMapper;
    }

    public GeneratorHandlerOutcomeDto start(@Valid GeneratorInitialValues generatorInitialValues) throws JsonProcessingException {
        final GeneratorHandlerOutcome outcome;
        if (generatorInitialValues != null) {
            outcome = generatorHandler.startAllTradeGenerators(generatorInitialValues);
        } else {
            outcome = generatorHandler.startAllTradeGenerators();
        }

        startConfirmationMatchRequestKafkaListener(outcome);

        GeneratorHandlerOutcomeDto outcomeDto = GeneratorHandlerOutcome.toDto(outcome);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outcomeDto));
        return outcomeDto;
    }

    private void startConfirmationMatchRequestKafkaListener(GeneratorHandlerOutcome outcome) {
        MessageListenerContainer container = kafkaListenerEndpointRegistry.getListenerContainer("confMatchRequestListener");
        if (container == null) {
            throw new RuntimeException("Unable to start kafka listener for ConfirmationMatchRequest events. No listenerContainer found with id: confMatchRequestListener");
        }

        switch (outcome) {
            case GeneratorHandlerOutcome.Success success -> {
                if (!container.isRunning()) {
                    kafkaConsumer.setConfirmationMatchEventTemplate(generatorHandler.confirmationMatchEventTemplate());
                    container.start();
                    log.info("Started kafka listener for consuming ConfirmationMatchRequest events");
                }
            }
            case GeneratorHandlerOutcome.Failure failure -> {
                log.info("No explicit action taken for ConfirmationMatchRequest kafka listener although the event generator startup has failed. isListenerRunning: {}", container.isRunning());
            }
            case GeneratorHandlerOutcome.ConcurrentOperation concurrentOperation -> {
            }
            case GeneratorHandlerOutcome.GenericMessage genericMessage -> {
            }
        }
    }

    /// Note: This method does not stop the ConfirmationMatchRequest kafka listener
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
