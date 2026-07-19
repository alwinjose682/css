package io.alw.css.tradepublisher.confirmation;

import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradepublisher.confirmation.mapper.ConfirmationMatchRequestMapper;
import io.alw.css.tradepublisher.confirmation.template.ConfirmationMatchEventTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final Supplier<ConfirmationMatchEventTemplate> confirmationMatchEventTemplateSupplier;
    private ConfirmationMatchEventTemplate confirmationMatchEventTemplate;

    public KafkaConsumer(Supplier<ConfirmationMatchEventTemplate> confirmationMatchEventTemplateSupplier) {
        this.confirmationMatchEventTemplateSupplier = confirmationMatchEventTemplateSupplier;
    }

    @KafkaListener(topics = "${app.kafka.topic.confirmation-match-request}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "confMatchRequestListenerContainerFactory")
    public void consume(Message<ConfirmationMatchRequestAvro> msg) {
        var matchRequestAvro = msg.getPayload();
        long tradeId = matchRequestAvro.getTradeId();
        int tradeVersion = matchRequestAvro.getTradeVersion();

        if(confirmationMatchEventTemplate==null){
            confirmationMatchEventTemplate = confirmationMatchEventTemplateSupplier.get();
        }

        log.info("Received ConfirmationMatchRequestAvro[tradeId: {}, tradeVersion: {}] for confirmation message generation and matching", tradeId, tradeVersion);
        ConfirmationMatchRequest req = ConfirmationMatchRequestMapper.instance().avroToDomain(matchRequestAvro);
        confirmationMatchEventTemplate.consume(req);
    }
}
