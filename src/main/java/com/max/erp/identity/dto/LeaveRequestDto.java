package com.max.erp.identity.dto;

import java.time.Instant;

public class LeaveRequestDto {
    private String id;
    private String employeeId;
    private String employeeName;
    private String department;
    private String leaveType; // VACATION, SICK, PARENTAL, BEREAVEMENT, UNPAID
    private String startDate; // YYYY-MM-DD
    private String endDate;   // YYYY-MM-DD
    private int totalDays;
    private String reason;
    private String status;    // PENDING, APPROVED, REJECTED, CANCELLED
    private String approvedBy;
    private String reviewNotes;
    private Instant requestedAt;
    private Instant reviewedAt;

    public LeaveRequestDto() {}

    public LeaveRequestDto(String id, String employeeId, String employeeName, String department,
                           String leaveType, String startDate, String endDate, int totalDays,
                           String reason, String status, String approvedBy, String reviewNotes,
                           Instant requestedAt, Instant reviewedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.reason = reason;
        this.status = status;
        this.approvedBy = approvedBy;
        this.reviewNotes = reviewNotes;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
