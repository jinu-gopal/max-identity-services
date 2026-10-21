package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FiscalGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(FiscalGovernanceService.class);

    private final String baseCurrency = "USD";
    private final Map<String, CurrencyExchangeRateDto> currenciesMap = new ConcurrentHashMap<>();
    private final List<FiscalPeriodDto> fiscalPeriods = new CopyOnWriteArrayList<>();
    private final List<TaxRuleDto> taxRules = new CopyOnWriteArrayList<>();

    public FiscalGovernanceService() {
        initializeCurrencies();
        initializeFiscalPeriods();
        initializeTaxRules();
    }

    private void initializeCurrencies() {
        currenciesMap.put("USD", new CurrencyExchangeRateDto("USD", "US Dollar", "$", 1.0, true, Instant.now()));
        currenciesMap.put("EUR", new CurrencyExchangeRateDto("EUR", "Euro", "€", 0.92, false, Instant.now()));
        currenciesMap.put("GBP", new CurrencyExchangeRateDto("GBP", "British Pound", "£", 0.77, false, Instant.now()));
        currenciesMap.put("JPY", new CurrencyExchangeRateDto("JPY", "Japanese Yen", "¥", 152.4, false, Instant.now()));
        currenciesMap.put("CAD", new CurrencyExchangeRateDto("CAD", "Canadian Dollar", "CA$", 1.38, false, Instant.now()));
        currenciesMap.put("AUD", new CurrencyExchangeRateDto("AUD", "Australian Dollar", "A$", 1.51, false, Instant.now()));
        currenciesMap.put("INR", new CurrencyExchangeRateDto("INR", "Indian Rupee", "₹", 84.1, false, Instant.now()));
        currenciesMap.put("SGD", new CurrencyExchangeRateDto("SGD", "Singapore Dollar", "S$", 1.31, false, Instant.now()));
        currenciesMap.put("CHF", new CurrencyExchangeRateDto("CHF", "Swiss Franc", "CHF", 0.86, false, Instant.now()));
    }

    private void initializeFiscalPeriods() {
        fiscalPeriods.add(new FiscalPeriodDto("period-1", "FY2026-Q1", "2026-01-01", "2026-03-31", "LOCKED", "Chief Financial Officer", Instant.now().minusSeconds(12000000)));
        fiscalPeriods.add(new FiscalPeriodDto("period-2", "FY2026-Q2", "2026-04-01", "2026-06-30", "LOCKED", "VP Finance & Controller", Instant.now().minusSeconds(6000000)));
        fiscalPeriods.add(new FiscalPeriodDto("period-3", "FY2026-Q3", "2026-07-01", "2026-09-30", "SOFT_CLOSED", "External Auditor", Instant.now().minusSeconds(1200000)));
        fiscalPeriods.add(new FiscalPeriodDto("period-4", "FY2026-Q4", "2026-10-01", "2026-12-31", "OPEN", null, null));
    }

    private void initializeTaxRules() {
        taxRules.add(new TaxRuleDto("tax-1", "California State & Local Sales Tax", "SALES_TAX", 7.25, "US-CALIFORNIA", "ACTIVE"));
        taxRules.add(new TaxRuleDto("tax-2", "Germany Standard VAT (Umsatzsteuer)", "VAT", 19.00, "EU-GERMANY", "ACTIVE"));
        taxRules.add(new TaxRuleDto("tax-3", "United Kingdom Standard Value Added Tax", "VAT", 20.00, "UK", "ACTIVE"));
        taxRules.add(new TaxRuleDto("tax-4", "Singapore Goods and Services Tax (GST)", "GST", 9.00, "APAC-SINGAPORE", "ACTIVE"));
        taxRules.add(new TaxRuleDto("tax-5", "India Integrated Goods and Services Tax (IGST)", "GST", 18.00, "IN-NATIONAL", "ACTIVE"));
    }

    public FinanceConfigSummaryDto getSummary() {
        List<CurrencyExchangeRateDto> currList = new ArrayList<>(currenciesMap.values());
        currList.sort(Comparator.comparing(CurrencyExchangeRateDto::isBase).reversed().thenComparing(CurrencyExchangeRateDto::getCurrencyCode));

        return new FinanceConfigSummaryDto(
                baseCurrency,
                currList,
                new ArrayList<>(fiscalPeriods),
                new ArrayList<>(taxRules)
        );
    }

    public CurrencyExchangeRateDto updateCurrencyRate(String code, double rate) {
        CurrencyExchangeRateDto existing = currenciesMap.get(code.toUpperCase());
        if (existing == null) return null;
        existing.setRateToBase(rate);
        existing.setUpdatedAt(Instant.now());
        log.info("Updated currency {} exchange rate to {}", code, rate);
        return existing;
    }

    public FiscalPeriodDto updatePeriodStatus(String periodId, String newStatus) {
        for (FiscalPeriodDto p : fiscalPeriods) {
            if (p.getId().equalsIgnoreCase(periodId)) {
                p.setStatus(newStatus.toUpperCase());
                if ("LOCKED".equalsIgnoreCase(newStatus) || "SOFT_CLOSED".equalsIgnoreCase(newStatus)) {
                    p.setClosedBy("Enterprise Finance Administrator");
                    p.setClosedAt(Instant.now());
                } else {
                    p.setClosedBy(null);
                    p.setClosedAt(null);
                }
                return p;
            }
        }
        return null;
    }

    public TaxRuleDto addTaxRule(TaxRuleDto rule) {
        String id = "tax-" + UUID.randomUUID().toString().substring(0, 8);
        rule.setId(id);
        taxRules.add(0, rule);
        return rule;
    }
}
