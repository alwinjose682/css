package io.alw.css.tradeconsumer.confirmation.model;

import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;

import java.util.List;

public record ConfirmationMatchRequestFactoryOutcome(
        ConfirmationMatchRequestAvro confMatchRequest,
        List<ConfirmationMatchStatusEntity> confMatchStatusJpaEntities
) {
}
