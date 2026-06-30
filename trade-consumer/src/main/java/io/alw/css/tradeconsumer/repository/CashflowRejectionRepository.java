package io.alw.css.tradeconsumer.repository;

import io.alw.css.tradeconsumer.model.jpa.CashflowRejectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashflowRejectionRepository extends JpaRepository<CashflowRejectionEntity, Long> {
}
