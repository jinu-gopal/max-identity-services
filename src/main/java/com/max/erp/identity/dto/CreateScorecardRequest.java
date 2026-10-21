package com.max.erp.identity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateScorecardRequest {

    @NotBlank(message = "Candidate ID is required")
    private String candidateId;

    @NotBlank(message = "Interviewer name is required")
    private String interviewerName;

    @NotBlank(message = "Round type is required")
    private String roundType = "TECHNICAL";

    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot exceed 5")
    private int score = 4;

    @NotBlank(message = "Recommendation is required")
    private String recommendation = "HIRE"; // STRONG_HIRE, HIRE, LEANING_NO, STRONG_NO

    private String feedbackSummary;

    public CreateScorecardRequest() {}

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

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
}
