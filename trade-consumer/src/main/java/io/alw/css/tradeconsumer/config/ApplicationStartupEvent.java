package io.alw.css.tradeconsumer.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.alw.css.tradeconsumer.model.FoCashflowIdAndTradeId;
import io.alw.css.tradeconsumer.model.generator.TradeGenerationInitialValues;
import io.alw.css.tradeconsumer.model.generator.TradeGeneratorStartResponse;
import io.alw.css.tradeconsumer.repository.CashflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

public class ApplicationStartupEvent implements ApplicationListener<ApplicationReadyEvent> {
    private final static Logger log = LoggerFactory.getLogger(ApplicationStartupEvent.class);

    @Value("${trade-generator.start-switch:#{null}}")
    private Boolean startAllTradeGenerators;
    @Value("${rest.services.trade-generator:#{null}}")
    private String tradeGeneratorUrl;

    private final RestTemplate restTemplate;
    private final CashflowRepository cashflowRepository;
    private final ObjectMapper objectMapper;

    public ApplicationStartupEvent(RestTemplate restTemplate, CashflowRepository cashflowRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.cashflowRepository = cashflowRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (startAllTradeGenerators == null || !startAllTradeGenerators) {
            log.info("Trade Generators of upstream trade-publisher are not started as per configuration. Trade Generators need to be started manually");
        } else if (tradeGeneratorUrl == null) {
            log.info("Trade Generators of upstream trade-publisher are not started because trade generator url is not configured. Trade Generators need to be started manually");
        } else {
            try {
                startAllTradeGenerators();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startAllTradeGenerators() throws JsonProcessingException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(tradeGeneratorUrl);
        URI uri = uriBuilder
                .pathSegment("start", "{key}")
                .build("all");

        var initValues = getTradeGenerationInitialValues();
        var req = RequestEntity
                .put(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(initValues);
        var res = restTemplate.exchange(req, TradeGeneratorStartResponse.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            var outcome = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res.getBody()); // Converting the response to json again to write a readable friendly string

            log.info("Started ALL Trade Generators in upstream/fo-simulator, {}", outcome);
        } else {
            log.warn("Failed to start Trade Generators in upstream/fo-simulator. Check the upstream simulator logs for details. REST Response: {}", res.getStatusCode());
        }
    }

    private TradeGenerationInitialValues getTradeGenerationInitialValues() {
        FoCashflowIdAndTradeId maxIds = cashflowRepository.findMaxTradeId();
        if (maxIds == null || maxIds.tradeId() == null) {
            var initValues = new TradeGenerationInitialValues(LocalDate.now(), 1054321L);
            log.info("Staring Trade Generators with initial values: {}", initValues);
            return initValues;
        }

        var initValues = new TradeGenerationInitialValues(LocalDate.now(), 1L + maxIds.tradeId());
        log.info("Staring Trade Generators with initial values greater than the values of last processed cashflow: {}", initValues);
        return initValues;
    }
}
