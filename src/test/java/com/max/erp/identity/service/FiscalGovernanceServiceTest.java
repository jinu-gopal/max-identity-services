package com.max.erp.identity.service;

import com.max.erp.identity.dto.CurrencyExchangeRateDto;
import com.max.erp.identity.dto.FinanceConfigSummaryDto;
import com.max.erp.identity.dto.FiscalPeriodDto;
import com.max.erp.identity.dto.TaxRuleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiscalGovernanceServiceTest {

    private FiscalGovernanceService fiscalService;

    @BeforeEach
    void setUp() {
        fiscalService = new FiscalGovernanceService();
    }

    @Test
    void testInitialFinanceSummary() {
        FinanceConfigSummaryDto summary = fiscalService.getSummary();
        assertNotNull(summary);
        assertEquals("USD", summary.getBaseCurrency());
        assertNotNull(summary.getCurrencies());
        assertTrue(summary.getCurrencies().size() >= 8);
        assertNotNull(summary.getFiscalPeriods());
        assertFalse(summary.getFiscalPeriods().isEmpty());
        assertNotNull(summary.getTaxRules());
        assertFalse(summary.getTaxRules().isEmpty());
    }

    @Test
    void testUpdateCurrencyRate() {
        CurrencyExchangeRateDto updated = fiscalService.updateCurrencyRate("EUR", 0.95);
        assertNotNull(updated);
        assertEquals("EUR", updated.getCurrencyCode());
        assertEquals(0.95, updated.getRateToBase());
    }

    @Test
    void testUpdateFiscalPeriodStatus() {
        FinanceConfigSummaryDto summary = fiscalService.getSummary();
        FiscalPeriodDto firstPeriod = summary.getFiscalPeriods().get(0);

        FiscalPeriodDto updated = fiscalService.updatePeriodStatus(firstPeriod.getId(), "LOCKED");
        assertNotNull(updated);
        assertEquals("LOCKED", updated.getStatus());
        assertNotNull(updated.getClosedBy());
    }

    @Test
    void testCreateTaxRule() {
        TaxRuleDto newRule = new TaxRuleDto(
                null,
                "State Green Eco Tax",
                "ECO_LEVY",
                2.50,
                "US-CALIFORNIA",
                "ACTIVE"
        );

        TaxRuleDto created = fiscalService.addTaxRule(newRule);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("State Green Eco Tax", created.getRuleName());
    }
}
