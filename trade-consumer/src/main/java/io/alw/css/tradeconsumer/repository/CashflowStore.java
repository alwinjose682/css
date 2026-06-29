package io.alw.css.tradeconsumer.repository;

import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import io.alw.css.tradeconsumer.model.jpa.CashflowEntity;
import io.alw.css.tradeconsumer.model.jpa.CashflowRejectionEntity;
import io.alw.css.tradeconsumer.model.jpa.TradeLinkEntity;
import io.alw.css.tradeconsumer.repository.mapper.CashflowMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public final class CashflowStore {
    private final static Logger log = LoggerFactory.getLogger(CashflowStore.class);

    @PersistenceContext
    private EntityManager em;
    private final CashflowRepository cashflowRepository;
    private final CashflowRejectionRepository cashflowRejectionRepository;
    private final TradeLinkRepository tradeLinkRepository;

    public CashflowStore(CashflowRepository cashflowRepository, CashflowRejectionRepository cashflowRejectionRepository, TradeLinkRepository tradeLinkRepository) {
        this.cashflowRepository = cashflowRepository;
        this.cashflowRejectionRepository = cashflowRejectionRepository;
        this.tradeLinkRepository = tradeLinkRepository;
    }

    /// **TODO**: When switching to Oracle DB, check whether Hibernate still returns Long
    ///
    /// **SUBTLE ISSUE**:
    ///
    /// When below property is set, the nextval of the sequence returned by Hibernate is of type java.lang.Long. When this property is not set, Hibernate returns java.math.BigDecimal
    /// - property: spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect
    /// - properties 'spring.jpa.database-platform' and 'spring.jpa.properties.hibernate.dialect' are for the same purpose and have the same effect
    ///
    /// Exception:
    /// class java.lang.Long cannot be cast to class java.math.BigDecimal (java.lang.Long and java.math.BigDecimal are in module java.base of loader 'bootstrap')
    ///
    /// **UPDATE**: Hibernate version 6.X has changed the mapping of DB type to Java type. (Looks like this applies ONLY for native queries)
    /// Check: https://discourse.hibernate.org/t/oracledialect-changes-in-number-type-mappings-in-version-6/7503
    /// Still, this difference in mapping happen only when enabling the said property!
    /// NOTE: JDBC still maps NUMBER to BigDecimal
    ///
    /// Example: Hibernate 6.X maps Oracle NUMBER type based on its width to java.lang.Integer, Long, BigDecimal etc. instead of the behaviour of old Hibernate versions that used to map oracle NUMBER to BigDecimal. Float types are mapped differently
    public long getNewCashflowID() {
        return ((BigDecimal) em.createNativeQuery("select CSS.cashflow_seq.nextval from dual").getSingleResult()).longValue();
//        return (long) em.createNativeQuery("select CSS.cashflow_seq.nextval from dual").getSingleResult();
    }

    /// This method returns null if no result. Does not use Optional
    public Cashflow getLastProcessedCashflow(long tradeId, long tradeLegId) {
        CashflowEntity lpcf = cashflowRepository.findLastProcessedCashflow(tradeId, tradeLegId);
        if (lpcf != null) {
            return CashflowMapper.instance().mapToDomain_excludingAssociations(lpcf);
        } else {
            return null;
        }
    }

    public void saveRejection(CashflowRejectionEntity cfr) {
        cashflowRejectionRepository.save(cfr);
    }

    /// This method does following actions atomically:
    /// 1. Update each last processed cashflow's 'latest' field to 'N' TODO: change this to a DB procedure to avoid multiple DB round trips
    /// 2. If exactly ONE row is updated in step 1, continues to step 3. If zero or more than 1 rows are updated, throws a [io.alw.css.domain.exception.CategorizedRuntimeException]
    /// 3. inserts the offset cashflow(CAN) and correction cashflow(COR) to DB. (Correction cashflow is created with latest='Y')
    public List<CashflowEntity> saveCashflows(List<Cashflow> newCashflows, List<Cashflow> lastProcessedCashflows) {
        // Step 1: Update last processed cashflow's 'latest' field to 'N'
        for (Cashflow lpcf : lastProcessedCashflows) {
            long lpcfId = lpcf.cashflowId();
            int lpcfVer = lpcf.cashflowVersion();
            int numOfRowsUpdated = cashflowRepository.updateLastProcessedCashflowToNonLatest(lpcfId, lpcfVer);

            if (numOfRowsUpdated == 1) {
                continue;
            } else if (numOfRowsUpdated == 0) {
                var errMsg = "Failed to update last processed cashflow possibly due to a concurrent update transaction. Last processed CashflowId-Ver[" + lpcf.cashflowId() + "-" + lpcf.cashflowVersion() + "]";
                log.error(errMsg);
                throw CategorizedRuntimeException.TECHNICAL_RECOVERABLE(errMsg, new ExceptionSubCategory(ExceptionSubCategoryType.CASHFLOW_PERSISTENCE_FAILURE, lpcf));
            } else { //if (numOfRowsUpdated > 1) {
                var errMsg = "Failed to update last processed cashflow. Multiple cashflows exist in database with latest='Y'. The cashflow is in invalid state and this should NOT happen. Last processed CashflowId-Ver[" + lpcf.cashflowId() + "-" + lpcf.cashflowVersion() + "]";
                log.error(errMsg);
                throw CategorizedRuntimeException.TECHNICAL_RECOVERABLE(errMsg, new ExceptionSubCategory(ExceptionSubCategoryType.CASHFLOW_PERSISTENCE_FAILURE, lpcf));
            }
        }

        // Exactly ONE row is updated. Therefore, persist the cashflows.
        return newCashflows.stream()
                .peek(cf -> log.trace("Saving Cashflow[{}-{}] to DB. TradeLeg[{}-{}]", cf.cashflowId(), cf.cashflowVersion(), cf.tradeLegId(), cf.tradeLegVersion()))
                .map(CashflowMapper::mapToEntity)
                .map(cashflowRepository::save)
                .toList();
    }

    public List<TradeLinkEntity> saveTradeLinks(List<TradeLinkEntity> tradeLinkEntities) {
        return tradeLinkEntities.stream().map(tradeLinkRepository::save).toList();
    }
}
