package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateLeaveStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // APPROVED, REJECTED, CANCELLED

    private String reviewNotes;
    private String approvedBy;

    public UpdateLeaveStatusRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
}
