package com.max.erp.identity.dto;

public class PositionSlotDto {
    private String slotId;
    private String slotCode; // e.g. REQ-ENG-084-POS1
    private String status;   // UNFILLED, INTERVIEWING, OFFER_OUT, FILLED
    private String assignedCandidateId;
    private String assignedCandidateName;

    public PositionSlotDto() {}

    public PositionSlotDto(String slotId, String slotCode, String status, String assignedCandidateId, String assignedCandidateName) {
        this.slotId = slotId;
        this.slotCode = slotCode;
        this.status = status;
        this.assignedCandidateId = assignedCandidateId;
        this.assignedCandidateName = assignedCandidateName;
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotCode() { return slotCode; }
    public void setSlotCode(String slotCode) { this.slotCode = slotCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedCandidateId() { return assignedCandidateId; }
    public void setAssignedCandidateId(String assignedCandidateId) { this.assignedCandidateId = assignedCandidateId; }

    public String getAssignedCandidateName() { return assignedCandidateName; }
    public void setAssignedCandidateName(String assignedCandidateName) { this.assignedCandidateName = assignedCandidateName; }
}
