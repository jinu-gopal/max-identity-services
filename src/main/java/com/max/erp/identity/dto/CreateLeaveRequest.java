package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CreateLeaveRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Leave type is required")
    private String leaveType = "VACATION"; // VACATION, SICK, PARENTAL, BEREAVEMENT, UNPAID

    @NotBlank(message = "Start date is required")
    private String startDate; // YYYY-MM-DD

    @NotBlank(message = "End date is required")
    private String endDate;   // YYYY-MM-DD

    @Positive(message = "Total days must be positive")
    private int totalDays = 1;

    private String reason;

    public CreateLeaveRequest() {}

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

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
}
