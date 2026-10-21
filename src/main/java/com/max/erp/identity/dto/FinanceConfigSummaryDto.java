package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.List;

public class FinanceConfigSummaryDto {
    private String baseCurrency;
    private List<CurrencyExchangeRateDto> currencies = new ArrayList<>();
    private List<FiscalPeriodDto> fiscalPeriods = new ArrayList<>();
    private List<TaxRuleDto> taxRules = new ArrayList<>();

    public FinanceConfigSummaryDto() {}

    public FinanceConfigSummaryDto(String baseCurrency, List<CurrencyExchangeRateDto> currencies,
                                  List<FiscalPeriodDto> fiscalPeriods, List<TaxRuleDto> taxRules) {
        this.baseCurrency = baseCurrency;
        this.currencies = currencies != null ? currencies : new ArrayList<>();
        this.fiscalPeriods = fiscalPeriods != null ? fiscalPeriods : new ArrayList<>();
        this.taxRules = taxRules != null ? taxRules : new ArrayList<>();
    }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public List<CurrencyExchangeRateDto> getCurrencies() { return currencies; }
    public void setCurrencies(List<CurrencyExchangeRateDto> currencies) { this.currencies = currencies; }

    public List<FiscalPeriodDto> getFiscalPeriods() { return fiscalPeriods; }
    public void setFiscalPeriods(List<FiscalPeriodDto> fiscalPeriods) { this.fiscalPeriods = fiscalPeriods; }

    public List<TaxRuleDto> getTaxRules() { return taxRules; }
    public void setTaxRules(List<TaxRuleDto> taxRules) { this.taxRules = taxRules; }
}
