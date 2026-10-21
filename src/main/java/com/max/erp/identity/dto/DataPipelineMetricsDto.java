package com.max.erp.identity.dto;

public class DataPipelineMetricsDto {
    private int totalJobs;
    private long totalRowsProcessed;
    private long totalSuccessfulRows;
    private long totalFailedRows;
    private double successRatePercent;
    private int activePipelines;

    public DataPipelineMetricsDto() {}

    public DataPipelineMetricsDto(int totalJobs, long totalRowsProcessed, long totalSuccessfulRows,
                                  long totalFailedRows, double successRatePercent, int activePipelines) {
        this.totalJobs = totalJobs;
        this.totalRowsProcessed = totalRowsProcessed;
        this.totalSuccessfulRows = totalSuccessfulRows;
        this.totalFailedRows = totalFailedRows;
        this.successRatePercent = successRatePercent;
        this.activePipelines = activePipelines;
    }

    public int getTotalJobs() { return totalJobs; }
    public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs; }

    public long getTotalRowsProcessed() { return totalRowsProcessed; }
    public void setTotalRowsProcessed(long totalRowsProcessed) { this.totalRowsProcessed = totalRowsProcessed; }

    public long getTotalSuccessfulRows() { return totalSuccessfulRows; }
    public void setTotalSuccessfulRows(long totalSuccessfulRows) { this.totalSuccessfulRows = totalSuccessfulRows; }

    public long getTotalFailedRows() { return totalFailedRows; }
    public void setTotalFailedRows(long totalFailedRows) { this.totalFailedRows = totalFailedRows; }

    public double getSuccessRatePercent() { return successRatePercent; }
    public void setSuccessRatePercent(double successRatePercent) { this.successRatePercent = successRatePercent; }

    public int getActivePipelines() { return activePipelines; }
    public void setActivePipelines(int activePipelines) { this.activePipelines = activePipelines; }
}
