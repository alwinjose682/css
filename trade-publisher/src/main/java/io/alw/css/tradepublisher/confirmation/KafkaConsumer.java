package io.alw.css.tradepublisher.confirmation;

import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradepublisher.confirmation.mapper.ConfirmationMatchRequestMapper;
import io.alw.css.tradepublisher.confirmation.template.ConfirmationMatchEventTemplate;
import io.alw.css.tradepublisher.generator.GeneratorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final GeneratorHandler generatorHandler;
    private ConfirmationMatchEventTemplate confirmationMatchEventTemplate; // LazyConstant

    public KafkaConsumer(GeneratorHandler generatorHandler) {
        this.generatorHandler = generatorHandler;
    }

    @KafkaListener(
            autoStartup = "false",
            id = "confMatchRequestListener",
            topics = "${app.kafka.topic.confirmation-match-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "confMatchRequestListenerContainerFactory")
    public void consume(Message<ConfirmationMatchRequestAvro> msg) {
        var matchRequestAvro = msg.getPayload();
        long tradeId = matchRequestAvro.getTradeId();
        int tradeVersion = matchRequestAvro.getTradeVersion();

        log.info("Received ConfirmationMatchRequestAvro[tradeId: {}, tradeVersion: {}] for confirmation message generation and matching", tradeId, tradeVersion);
        ConfirmationMatchRequest req = ConfirmationMatchRequestMapper.instance().avroToDomain(matchRequestAvro);
        confirmationMatchEventTemplate.consume(req);
    }

    // Assigns if not already assigned
    public void setConfirmationMatchEventTemplate(ConfirmationMatchEventTemplate confirmationMatchEventTemplate) {
        if (this.confirmationMatchEventTemplate == null) {
            this.confirmationMatchEventTemplate = confirmationMatchEventTemplate;
        }
    }
}
