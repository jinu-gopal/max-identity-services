package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateCandidateStageRequest {

    @NotBlank(message = "Stage is required")
    private String stage; // APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED

    private String notes;

    public UpdateCandidateStageRequest() {}

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
