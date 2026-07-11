package io.alw.css.tradeconsumer.confirmation.repository;

import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationMatchStatusRepository extends JpaRepository<ConfirmationMatchStatusEntity, Long> {
}
