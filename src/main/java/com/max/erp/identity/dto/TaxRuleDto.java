package com.max.erp.identity.dto;

public class TaxRuleDto {
    private String id;
    private String ruleName;
    private String taxType; // VAT, GST, SALES_TAX, CUSTOMS
    private double ratePercent;
    private String applicableRegion;
    private String status; // ACTIVE, INACTIVE

    public TaxRuleDto() {}

    public TaxRuleDto(String id, String ruleName, String taxType, double ratePercent, String applicableRegion, String status) {
        this.id = id;
        this.ruleName = ruleName;
        this.taxType = taxType;
        this.ratePercent = ratePercent;
        this.applicableRegion = applicableRegion;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getTaxType() { return taxType; }
    public void setTaxType(String taxType) { this.taxType = taxType; }

    public double getRatePercent() { return ratePercent; }
    public void setRatePercent(double ratePercent) { this.ratePercent = ratePercent; }

    public String getApplicableRegion() { return applicableRegion; }
    public void setApplicableRegion(String applicableRegion) { this.applicableRegion = applicableRegion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
