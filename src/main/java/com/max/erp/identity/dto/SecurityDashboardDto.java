package com.max.erp.identity.dto;

public class SecurityDashboardDto {

    private int healthScore;
    private long activeSessionsCount;
    private long totalAuditEventsCount;
    private long totalRolesCount;
    private long totalPermissionsCount;
    private long totalUsersCount;
    private int mfaEnabledPercent;
    private long approvedWorkflowsCount;
    private long rejectedWorkflowsCount;
    private long pendingWorkflowsCount;

    public SecurityDashboardDto() {}

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public long getActiveSessionsCount() {
        return activeSessionsCount;
    }

    public void setActiveSessionsCount(long activeSessionsCount) {
        this.activeSessionsCount = activeSessionsCount;
    }

    public long getTotalAuditEventsCount() {
        return totalAuditEventsCount;
    }

    public void setTotalAuditEventsCount(long totalAuditEventsCount) {
        this.totalAuditEventsCount = totalAuditEventsCount;
    }

    public long getTotalRolesCount() {
        return totalRolesCount;
    }

    public void setTotalRolesCount(long totalRolesCount) {
        this.totalRolesCount = totalRolesCount;
    }

    public long getTotalPermissionsCount() {
        return totalPermissionsCount;
    }

    public void setTotalPermissionsCount(long totalPermissionsCount) {
        this.totalPermissionsCount = totalPermissionsCount;
    }

    public long getTotalUsersCount() {
        return totalUsersCount;
    }

    public void setTotalUsersCount(long totalUsersCount) {
        this.totalUsersCount = totalUsersCount;
    }

    public int getMfaEnabledPercent() {
        return mfaEnabledPercent;
    }

    public void setMfaEnabledPercent(int mfaEnabledPercent) {
        this.mfaEnabledPercent = mfaEnabledPercent;
    }

    public long getApprovedWorkflowsCount() {
        return approvedWorkflowsCount;
    }

    public void setApprovedWorkflowsCount(long approvedWorkflowsCount) {
        this.approvedWorkflowsCount = approvedWorkflowsCount;
    }

    public long getRejectedWorkflowsCount() {
        return rejectedWorkflowsCount;
    }

    public void setRejectedWorkflowsCount(long rejectedWorkflowsCount) {
        this.rejectedWorkflowsCount = rejectedWorkflowsCount;
    }

    public long getPendingWorkflowsCount() {
        return pendingWorkflowsCount;
    }

    public void setPendingWorkflowsCount(long pendingWorkflowsCount) {
        this.pendingWorkflowsCount = pendingWorkflowsCount;
    }
}
