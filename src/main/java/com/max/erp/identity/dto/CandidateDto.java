package com.max.erp.identity.dto;

import java.time.Instant;

public class CandidateDto {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String requisitionId;
    private String requisitionTitle;
    private String department;
    private String stage; // APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED
    private String source; // LINKEDIN, REFERRAL, CAREERS_PORTAL, HEADHUNTER, CAMPUS
    private String resumeSummary;
    private String portfolioUrl;
    private double interviewScore; // 1.0 to 5.0
    private String notes;
    private Instant appliedAt;
    private Instant lastActivityAt;

    public CandidateDto() {}

    public CandidateDto(String id, String fullName, String email, String phone,
                        String requisitionId, String requisitionTitle, String department,
                        String stage, String source, String resumeSummary,
                        String portfolioUrl, double interviewScore, String notes,
                        Instant appliedAt, Instant lastActivityAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.requisitionId = requisitionId;
        this.requisitionTitle = requisitionTitle;
        this.department = department;
        this.stage = stage;
        this.source = source;
        this.resumeSummary = resumeSummary;
        this.portfolioUrl = portfolioUrl;
        this.interviewScore = interviewScore;
        this.notes = notes;
        this.appliedAt = appliedAt;
        this.lastActivityAt = lastActivityAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRequisitionId() { return requisitionId; }
    public void setRequisitionId(String requisitionId) { this.requisitionId = requisitionId; }

    public String getRequisitionTitle() { return requisitionTitle; }
    public void setRequisitionTitle(String requisitionTitle) { this.requisitionTitle = requisitionTitle; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getResumeSummary() { return resumeSummary; }
    public void setResumeSummary(String resumeSummary) { this.resumeSummary = resumeSummary; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public double getInterviewScore() { return interviewScore; }
    public void setInterviewScore(double interviewScore) { this.interviewScore = interviewScore; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }
}
