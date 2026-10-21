package com.max.erp.identity.dto;

import java.time.Instant;

public class CurrencyExchangeRateDto {
    private String currencyCode;
    private String currencyName;
    private String symbol;
    private double rateToBase;
    private boolean isBase;
    private Instant updatedAt;

    public CurrencyExchangeRateDto() {}

    public CurrencyExchangeRateDto(String currencyCode, String currencyName, String symbol, double rateToBase, boolean isBase, Instant updatedAt) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.symbol = symbol;
        this.rateToBase = rateToBase;
        this.isBase = isBase;
        this.updatedAt = updatedAt;
    }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getRateToBase() { return rateToBase; }
    public void setRateToBase(double rateToBase) { this.rateToBase = rateToBase; }

    public boolean isBase() { return isBase; }
    public void setBase(boolean base) { isBase = base; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
