package com.max.erp.identity.dto;

import java.time.Instant;

public class InterviewScorecardDto {
    private String id;
    private String candidateId;
    private String candidateName;
    private String requisitionId;
    private String interviewerName;
    private String roundType; // SCREENING, TECHNICAL, SYSTEM_DESIGN, CULTURE_FIT, EXECUTIVE
    private int score; // 1 to 5
    private String recommendation; // STRONG_HIRE, HIRE, LEANING_NO, STRONG_NO
    private String feedbackSummary;
    private Instant conductedAt;

    public InterviewScorecardDto() {}

    public InterviewScorecardDto(String id, String candidateId, String candidateName,
                                 String requisitionId, String interviewerName, String roundType,
                                 int score, String recommendation, String feedbackSummary,
                                 Instant conductedAt) {
        this.id = id;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.requisitionId = requisitionId;
        this.interviewerName = interviewerName;
        this.roundType = roundType;
        this.score = score;
        this.recommendation = recommendation;
        this.feedbackSummary = feedbackSummary;
        this.conductedAt = conductedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getRequisitionId() { return requisitionId; }
    public void setRequisitionId(String requisitionId) { this.requisitionId = requisitionId; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getRoundType() { return roundType; }
    public void setRoundType(String roundType) { this.roundType = roundType; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getFeedbackSummary() { return feedbackSummary; }
    public void setFeedbackSummary(String feedbackSummary) { this.feedbackSummary = feedbackSummary; }

    public Instant getConductedAt() { return conductedAt; }
    public void setConductedAt(Instant conductedAt) { this.conductedAt = conductedAt; }
}
