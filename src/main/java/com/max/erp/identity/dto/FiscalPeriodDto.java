package com.max.erp.identity.dto;

import java.time.Instant;

public class FiscalPeriodDto {
    private String id;
    private String periodName;
    private String startDate;
    private String endDate;
    private String status; // OPEN, SOFT_CLOSED, LOCKED
    private String closedBy;
    private Instant closedAt;

    public FiscalPeriodDto() {}

    public FiscalPeriodDto(String id, String periodName, String startDate, String endDate, String status, String closedBy, Instant closedAt) {
        this.id = id;
        this.periodName = periodName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.closedBy = closedBy;
        this.closedAt = closedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
