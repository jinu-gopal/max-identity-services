package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CreateShiftRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Shift date is required")
    private String shiftDate; // YYYY-MM-DD

    @NotBlank(message = "Shift type is required")
    private String shiftType = "MORNING"; // MORNING, EVENING, NIGHT, ON_CALL

    private String startTime = "08:00";
    private String endTime = "16:30";

    @Positive(message = "Scheduled hours must be positive")
    private double scheduledHours = 8.0;

    private String location = "MAIN_PLANT";

    public CreateShiftRequest() {}

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getShiftDate() { return shiftDate; }
    public void setShiftDate(String shiftDate) { this.shiftDate = shiftDate; }

    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public double getScheduledHours() { return scheduledHours; }
    public void setScheduledHours(double scheduledHours) { this.scheduledHours = scheduledHours; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
