package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JobRequisitionDto {
    private String id;
    private String reqNumber;
    private String title;
    private String department;
    private String jobLevel; // ENTRY, MID, SENIOR, LEAD, DIRECTOR, EXECUTIVE
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT
    private String location; // HEADQUARTERS, REMOTE, REGIONAL_HUB
    private int targetHeadcount;
    private int filledHeadcount;
    private String salaryRange; // e.g. "$140,000 - $175,000"
    private String hiringManagerName;
    private String status; // DRAFT, OPEN, INTERVIEWING, FILLED, ON_HOLD
    private List<String> requiredSkills = new ArrayList<>();
    private Instant createdAt;

    // Org Chart & Position Linking
    private String orgUnitId;
    private String orgUnitName;
    private String positionId;

    // Multi-stage Approval Workflow
    private String approvalStatus = "APPROVED"; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED
    private int currentApprovalStep = 3; // 1: Manager, 2: Finance, 3: HR Sign-off
    private List<String> approverNames = new ArrayList<>();

    // Multi-Position Slot Differentiation
    private List<PositionSlotDto> positionSlots = new ArrayList<>();

    public JobRequisitionDto() {}

    public JobRequisitionDto(String id, String reqNumber, String title, String department,
                             String jobLevel, String employmentType, String location,
                             int targetHeadcount, int filledHeadcount, String salaryRange,
                             String hiringManagerName, String status, List<String> requiredSkills,
                             Instant createdAt) {
        this.id = id;
        this.reqNumber = reqNumber;
        this.title = title;
        this.department = department;
        this.jobLevel = jobLevel;
        this.employmentType = employmentType;
        this.location = location;
        this.targetHeadcount = targetHeadcount;
        this.filledHeadcount = filledHeadcount;
        this.salaryRange = salaryRange;
        this.hiringManagerName = hiringManagerName;
        this.status = status;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.createdAt = createdAt;

        // Auto-generate position slots based on target headcount
        this.positionSlots = new ArrayList<>();
        for (int i = 1; i <= targetHeadcount; i++) {
            String slotCode = reqNumber + "-POS" + i;
            String slotStatus = i <= filledHeadcount ? "FILLED" : "UNFILLED";
            positionSlots.add(new PositionSlotDto("slot-" + id + "-" + i, slotCode, slotStatus, null, null));
        }

        this.approvalStatus = "APPROVED";
        this.currentApprovalStep = 3;
        this.approverNames = List.of("Marcus Chen (Hiring Lead)", "Rachel Green (Finance VP)", "Sarah Jenkins (HR Director)");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReqNumber() { return reqNumber; }
    public void setReqNumber(String reqNumber) { this.reqNumber = reqNumber; }

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

    public int getFilledHeadcount() { return filledHeadcount; }
    public void setFilledHeadcount(int filledHeadcount) { this.filledHeadcount = filledHeadcount; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getHiringManagerName() { return hiringManagerName; }
    public void setHiringManagerName(String hiringManagerName) { this.hiringManagerName = hiringManagerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(String orgUnitId) { this.orgUnitId = orgUnitId; }

    public String getOrgUnitName() { return orgUnitName; }
    public void setOrgUnitName(String orgUnitName) { this.orgUnitName = orgUnitName; }

    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public int getCurrentApprovalStep() { return currentApprovalStep; }
    public void setCurrentApprovalStep(int currentApprovalStep) { this.currentApprovalStep = currentApprovalStep; }

    public List<String> getApproverNames() { return approverNames; }
    public void setApproverNames(List<String> approverNames) { this.approverNames = approverNames; }

    public List<PositionSlotDto> getPositionSlots() { return positionSlots; }
    public void setPositionSlots(List<PositionSlotDto> positionSlots) { this.positionSlots = positionSlots; }
}
