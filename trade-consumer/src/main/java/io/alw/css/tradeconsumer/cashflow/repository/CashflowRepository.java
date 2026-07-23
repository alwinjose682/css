package io.alw.css.tradeconsumer.cashflow.repository;

import io.alw.css.tradeconsumer.cashflow.model.GeneratorIds;
import io.alw.css.tradeconsumer.cashflow.model.jpa.CashflowEntity;
import io.alw.css.tradeconsumer.cashflow.model.jpa.CashflowEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CashflowRepository extends JpaRepository<CashflowEntity, CashflowEntityPK> {

    @Query(value = """
            select cf from CashflowEntity cf
            where cf.tradeId=:tradeId and cf.tradeLegId=:tradeLegId
            and cf.tradeLegVersion in (select max(cf1.tradeLegVersion) from CashflowEntity cf1 where cf1.tradeId=cf.tradeId and cf1.tradeLegId=cf.tradeLegId)
            """)
    CashflowEntity findPreviousVersionCashflow(long tradeId, long tradeLegId);

    @Modifying
    @Query(value = """
            update CashflowEntity cf
            set cf.latest = 'N'
            where cf.cashflowEntityPK.cashflowId = :cashflowId and cf.cashflowEntityPK.cashflowVersion = :cashflowVersion and cf.latest = 'Y'
            """)
    int updatePreviousVersionCashflowToNonLatest(@Param("cashflowId") long cashflowId, @Param("cashflowVersion") int cashflowVersion);

    @Query(value = """
                select A.*, B.* from
                (select MAX(cf.trade_id) as tradeId FROM cashflow cf) A
                CROSS JOIN
                (select MAX(cms.match_event_id) as matchEventId FROM confirmation_match_status cms) B
            """, nativeQuery = true)
    GeneratorIds findMaxTradeId();
}
