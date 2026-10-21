package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

public class CreateJobRequisitionRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Department is required")
    private String department;

    private String jobLevel = "MID";
    private String employmentType = "FULL_TIME";
    private String location = "HEADQUARTERS";

    @Positive(message = "Headcount must be positive")
    private int targetHeadcount = 1;

    private String salaryRange = "$120,000 - $150,000";
    private String hiringManagerName;
    private List<String> requiredSkills = new ArrayList<>();

    // Org Chart & Position integration
    private String orgUnitId;
    private String orgUnitName;
    private String positionId;
    private String approvalStatus;

    public CreateJobRequisitionRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getJobLevel() { return jobLevel; }
    public void setJobLevel(String jobLevel) { this.jobLevel = jobLevel; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getTargetHeadcount() { return targetHeadcount; }
    public void setTargetHeadcount(int targetHeadcount) { this.targetHeadcount = targetHeadcount; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getHiringManagerName() { return hiringManagerName; }
    public void setHiringManagerName(String hiringManagerName) { this.hiringManagerName = hiringManagerName; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(String orgUnitId) { this.orgUnitId = orgUnitId; }

    public String getOrgUnitName() { return orgUnitName; }
    public void setOrgUnitName(String orgUnitName) { this.orgUnitName = orgUnitName; }

    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
}
