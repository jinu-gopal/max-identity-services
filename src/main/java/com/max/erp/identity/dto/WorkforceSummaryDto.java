package com.max.erp.identity.dto;

import java.util.List;
import java.util.Map;

public class WorkforceSummaryDto {
    private int totalEmployees;
    private int activeEmployees;
    private int onLeaveEmployees;
    private int onDutyToday;
    private int pendingLeaveRequests;
    private double overallAttendanceRate;
    private Map<String, Integer> departmentHeadcount;
    private List<EmployeeDto> leadershipTeam;
    private List<ShiftAssignmentDto> todaysShifts;
    private List<LeaveRequestDto> recentLeaves;

    public WorkforceSummaryDto() {}

    public WorkforceSummaryDto(int totalEmployees, int activeEmployees, int onLeaveEmployees,
                               int onDutyToday, int pendingLeaveRequests, double overallAttendanceRate,
                               Map<String, Integer> departmentHeadcount, List<EmployeeDto> leadershipTeam,
                               List<ShiftAssignmentDto> todaysShifts, List<LeaveRequestDto> recentLeaves) {
        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.onLeaveEmployees = onLeaveEmployees;
        this.onDutyToday = onDutyToday;
        this.pendingLeaveRequests = pendingLeaveRequests;
        this.overallAttendanceRate = overallAttendanceRate;
        this.departmentHeadcount = departmentHeadcount;
        this.leadershipTeam = leadershipTeam;
        this.todaysShifts = todaysShifts;
        this.recentLeaves = recentLeaves;
    }

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

    public int getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(int activeEmployees) { this.activeEmployees = activeEmployees; }

    public int getOnLeaveEmployees() { return onLeaveEmployees; }
    public void setOnLeaveEmployees(int onLeaveEmployees) { this.onLeaveEmployees = onLeaveEmployees; }

    public int getOnDutyToday() { return onDutyToday; }
    public void setOnDutyToday(int onDutyToday) { this.onDutyToday = onDutyToday; }

    public int getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(int pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }

    public double getOverallAttendanceRate() { return overallAttendanceRate; }
    public void setOverallAttendanceRate(double overallAttendanceRate) { this.overallAttendanceRate = overallAttendanceRate; }

    public Map<String, Integer> getDepartmentHeadcount() { return departmentHeadcount; }
    public void setDepartmentHeadcount(Map<String, Integer> departmentHeadcount) { this.departmentHeadcount = departmentHeadcount; }

    public List<EmployeeDto> getLeadershipTeam() { return leadershipTeam; }
    public void setLeadershipTeam(List<EmployeeDto> leadershipTeam) { this.leadershipTeam = leadershipTeam; }

    public List<ShiftAssignmentDto> getTodaysShifts() { return todaysShifts; }
    public void setTodaysShifts(List<ShiftAssignmentDto> todaysShifts) { this.todaysShifts = todaysShifts; }

    public List<LeaveRequestDto> getRecentLeaves() { return recentLeaves; }
    public void setRecentLeaves(List<LeaveRequestDto> recentLeaves) { this.recentLeaves = recentLeaves; }
}
