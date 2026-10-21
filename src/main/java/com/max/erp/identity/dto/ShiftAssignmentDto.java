package com.max.erp.identity.dto;

import java.time.Instant;

public class ShiftAssignmentDto {
    private String id;
    private String employeeId;
    private String employeeName;
    private String department;
    private String shiftDate; // YYYY-MM-DD
    private String shiftType; // MORNING, EVENING, NIGHT, ON_CALL
    private String startTime; // 08:00
    private String endTime;   // 16:30
    private double scheduledHours;
    private String location;  // MAIN_PLANT, DC_NORTH, REMOTE
    private String status;    // SCHEDULED, CLOCKED_IN, COMPLETED, ABSENT
    private Instant createdAt;

    public ShiftAssignmentDto() {}

    public ShiftAssignmentDto(String id, String employeeId, String employeeName,
                              String department, String shiftDate, String shiftType,
                              String startTime, String endTime, double scheduledHours,
                              String location, String status, Instant createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduledHours = scheduledHours;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
