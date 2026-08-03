package io.alw.css.tradeconsumer.cashflow.repository;

import io.alw.css.tradeconsumer.cashflow.model.jpa.TradeLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeLinkRepository extends JpaRepository<TradeLinkEntity, Long> {
}
