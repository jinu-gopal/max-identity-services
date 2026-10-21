package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.List;

public class AiInsightsDto {
    private int healthIndex;
    private String executiveSummary;
    private String approvalVelocity;
    private List<String> bottlenecks = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();

    public AiInsightsDto() {}

    public AiInsightsDto(int healthIndex, String executiveSummary, String approvalVelocity,
                         List<String> bottlenecks, List<String> recommendations) {
        this.healthIndex = healthIndex;
        this.executiveSummary = executiveSummary;
        this.approvalVelocity = approvalVelocity;
        this.bottlenecks = bottlenecks != null ? bottlenecks : new ArrayList<>();
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public int getHealthIndex() { return healthIndex; }
    public void setHealthIndex(int healthIndex) { this.healthIndex = healthIndex; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public String getApprovalVelocity() { return approvalVelocity; }
    public void setApprovalVelocity(String approvalVelocity) { this.approvalVelocity = approvalVelocity; }

    public List<String> getBottlenecks() { return bottlenecks; }
    public void setBottlenecks(List<String> bottlenecks) { this.bottlenecks = bottlenecks; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}
