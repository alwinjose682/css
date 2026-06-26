package io.alw.css.cashflowconsumer.repository;

import io.alw.css.cashflowconsumer.model.jpa.TradeLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeLinkRepository extends JpaRepository<TradeLinkEntity, Long> {
}
