package io.alw.css.tradeconsumer.cashflow.repository;

import io.alw.css.tradeconsumer.cashflow.model.jpa.CashflowRejectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashflowRejectionRepository extends JpaRepository<CashflowRejectionEntity, Long> {
}
