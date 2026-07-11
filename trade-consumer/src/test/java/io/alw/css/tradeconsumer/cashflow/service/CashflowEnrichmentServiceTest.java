package io.alw.css.tradeconsumer.cashflow.service;

import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.*;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionCategory;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.domain.exception.ExceptionType;
import io.alw.css.tradeconsumer.cashflow.model.SsiWithCounterpartyData;
import io.alw.css.tradeconsumer.cashflow.model.properties.SuppressionConfig;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import io.alw.css.tradeconsumer.service.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
@SpringBootTest(properties = "spring.main.lazy-initialization=true") -- This is ok due to lazy initialization, but still loads spring context
 */
// -- OR --
/*
@SpringBootTest(classes = {CashflowEnricher.class})
@EnableConfigurationProperties(value = SuppressionConfig.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
 */
// -- OR --
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = SuppressionConfig.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = CashflowEnrichmentService.class)
//@TestPropertySource("classpath:application.yml") -- This annotation does not support loading yaml files
//
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
//@ActiveProfiles({"test"})
class CashflowEnrichmentServiceTest {

    @MockitoBean
    CacheService cacheService;

    @Autowired
    CashflowEnrichmentService cashflowEnrichmentService;

    @Test
    void testEntityAndCurrCodeValidation() {
        var entityCode = "LON";
        var currCode = "GBP";
        CashflowBuilder builder = CashflowBuilder.builder(genericCashflow())
                .entityCode(entityCode)
                .currCode(currCode);

        // When both entity and currency are ACTIVE
        when(cacheService.isEntityActive(entityCode)).thenReturn(true);
        when(cacheService.isCurrencyActive(currCode)).thenReturn(true);
        assertDoesNotThrow(() -> cashflowEnrichmentService.validateEntityAndCurrCode(builder));

        // When entity is INACTIVE and currency is ACTIVE
        when(cacheService.isEntityActive(entityCode)).thenReturn(false);
        when(cacheService.isCurrencyActive(currCode)).thenReturn(true);
        CategorizedRuntimeException entityIsInactiveException = assertThrows(CategorizedRuntimeException.class, () -> cashflowEnrichmentService.validateEntityAndCurrCode(builder));
        assertEquals(ExceptionType.BUSINESS, entityIsInactiveException.type());
        assertEquals(ExceptionCategory.RECOVERABLE, entityIsInactiveException.category());
        assertEquals(new ExceptionSubCategory(ExceptionSubCategoryType.INACTIVE_ENTITY, null).type(), entityIsInactiveException.subCategory().type());

        // When entity is ACTIVE and currency is INACTIVE
        when(cacheService.isEntityActive(entityCode)).thenReturn(true);
        when(cacheService.isCurrencyActive(currCode)).thenReturn(false);
        CategorizedRuntimeException currencyIsInactiveException = assertThrows(CategorizedRuntimeException.class, () -> cashflowEnrichmentService.validateEntityAndCurrCode(builder));
        assertEquals(ExceptionType.BUSINESS, currencyIsInactiveException.type());
        assertEquals(ExceptionCategory.RECOVERABLE, currencyIsInactiveException.category());
        assertEquals(new ExceptionSubCategory(ExceptionSubCategoryType.INACTIVE_CURRENCY, null).type(), currencyIsInactiveException.subCategory().type());
    }

    @Test
    void testPaymentSuppressionWhenInterbookCashflow() {
        SuppressionConfig suppressionConfigMock = mock(SuppressionConfig.class);
        when(suppressionConfigMock.suppressInterbookTX()).thenReturn(true);
        // Create an interbook cashflow
        CashflowBuilder builder = CashflowBuilder.builder(genericCashflow())
                .paymentSuppressionCategory(null)
                .transactionType(TransactionType.INTER_BOOK);

        // Create a distinct CashflowEnricher instance with mocked SuppressionConfig
        CashflowEnrichmentService cfEnricher = new CashflowEnrichmentService(suppressionConfigMock, cacheService);
        cfEnricher.setPaymentSuppressionValue(builder);

        assertEquals(PaymentSuppressionCategory.INTERBOOK, builder.paymentSuppressionCategory());
    }

    @Test
    void testInternalValueAssignment() {
        SsiWithCounterpartyData cpData = mock(SsiWithCounterpartyData.class);

        {
            final var builder = CashflowBuilder.builder(genericCashflow()).transactionType(TransactionType.INTER_COMPANY);
            // When transaction type IS INTER_COMPANY and counterparty IS internal
            when(cpData.internal()).thenReturn(true);
            cashflowEnrichmentService.setInternalValue(builder, cpData);
            assertTrue(builder.internal());

            // When transaction type IS INTER_COMPANY and counterparty is NOT internal
            when(cpData.internal()).thenReturn(false);
            cashflowEnrichmentService.setInternalValue(builder, cpData);
            assertFalse(builder.internal());
        }

        {
            // When transaction type IS CORPORATE_ACTION and counterparty IS internal
            final var builder2 = CashflowBuilder.builder(genericCashflow()).transactionType(TransactionType.CORPORATE_ACTION);
            when(cpData.internal()).thenReturn(true);
            cashflowEnrichmentService.setInternalValue(builder2, cpData);
            assertFalse(builder2.internal());

            // When transaction type IS CORPORATE_ACTION and counterparty is NOT internal
            when(cpData.internal()).thenReturn(false);
            cashflowEnrichmentService.setInternalValue(builder2, cpData);
            assertFalse(builder2.internal());
        }
    }

    @Test
    void testPaymentSuppressionWhenNonSuppressiblePAYCashflow() {
        // Create a cashflow that has properties which makes it eligible for suppression check
        CashflowBuilder builder = CashflowBuilder.builder(genericCashflow())
                .paymentSuppressionCategory(null)
                .transactionType(TransactionType.MARKET)
                .currCode("INR")
                .amount(new BigDecimal("-33755.10005"));
        cashflowEnrichmentService.setPaymentSuppressionValue(builder);

        assertEquals(PaymentSuppressionCategory.NONE, builder.paymentSuppressionCategory());
    }

    @Test
    void testPaymentSuppressionWhenSuppressiblePAYCashflow() {
        CashflowBuilder builder = CashflowBuilder.builder(genericCashflow())
                .paymentSuppressionCategory(null)
                .transactionType(TransactionType.MARKET)
                .currCode("INR")
                .amount(new BigDecimal("-09.10005"));
        cashflowEnrichmentService.setPaymentSuppressionValue(builder);

        assertEquals(PaymentSuppressionCategory.AMOUNT_TOO_SMALL, builder.paymentSuppressionCategory());
    }

    @Test
    void testPaymentSuppressionWhenNonSuppressibleRECCashflow() {
        CashflowBuilder builder = CashflowBuilder.builder(genericCashflow())
                .paymentSuppressionCategory(null)
                .transactionType(TransactionType.MARKET)
                .currCode("USD")
                .amount(new BigDecimal("1.1"));
        cashflowEnrichmentService.setPaymentSuppressionValue(builder);

        assertEquals(PaymentSuppressionCategory.NONE, builder.paymentSuppressionCategory());
    }

    @Test
    void testPaymentSuppressionWhenSuppressibleRECCashflow() {
        CashflowBuilder builder = CashflowBuilder.builder(genericCashflow())
                .paymentSuppressionCategory(null)
                .transactionType(TransactionType.MARKET)
                .currCode("USD")
                .amount(new BigDecimal("1.0"));
        cashflowEnrichmentService.setPaymentSuppressionValue(builder);

        assertEquals(PaymentSuppressionCategory.AMOUNT_TOO_SMALL, builder.paymentSuppressionCategory());
    }

    private Cashflow genericCashflow() {
        return CashflowBuilder.builder()
                .cashflowId(16540)
                .cashflowVersion(1)
                .latest(true)
                .revisionType(RevisionType.NEW)
                .tradeId(13703)
                .tradeVersion(1)
                .tradeLegId(1)
                .tradeLegVersion(1)
                .tradeType(TradeType.BOND)
                .bookCode("DUMY")
                .counterBookCode(null)
                .transactionType(TransactionType.MARKET)
                .rate(new BigDecimal("1.2154754"))
                .valueDate(LocalDate.now().plusDays(1))
                .entityCode("DEL")
                .counterpartyCode("CP002967")
                .amount(new BigDecimal("-33755.10005"))
                .currCode("INR")
                .internal(false)
                .nostroId("ncl")
                .ssiId("a1izu")
                .paymentSuppressionCategory(PaymentSuppressionCategory.NONE)
                .inputBy(InputBy.CSS_SYS)
                .inputByUserId(null)
                .inputDateTime(LocalDateTime.now())
                .build()
                ;
    }
}
