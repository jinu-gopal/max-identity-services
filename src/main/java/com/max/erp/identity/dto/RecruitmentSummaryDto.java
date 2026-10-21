package com.max.erp.identity.dto;

import java.util.List;
import java.util.Map;

public class RecruitmentSummaryDto {
    private int openRequisitions;
    private int totalCandidatesInPipeline;
    private int activeInterviews;
    private int offersExtended;
    private int hiredThisQuarter;
    private double offerAcceptanceRate;
    private int averageDaysToHire;
    private Map<String, Integer> candidatesByStage;
    private Map<String, Integer> requisitionsByDepartment;
    private List<JobRequisitionDto> priorityRequisitions;
    private List<CandidateDto> recentApplicants;

    public RecruitmentSummaryDto() {}

    public RecruitmentSummaryDto(int openRequisitions, int totalCandidatesInPipeline,
                                 int activeInterviews, int offersExtended, int hiredThisQuarter,
                                 double offerAcceptanceRate, int averageDaysToHire,
                                 Map<String, Integer> candidatesByStage,
                                 Map<String, Integer> requisitionsByDepartment,
                                 List<JobRequisitionDto> priorityRequisitions,
                                 List<CandidateDto> recentApplicants) {
        this.openRequisitions = openRequisitions;
        this.totalCandidatesInPipeline = totalCandidatesInPipeline;
        this.activeInterviews = activeInterviews;
        this.offersExtended = offersExtended;
        this.hiredThisQuarter = hiredThisQuarter;
        this.offerAcceptanceRate = offerAcceptanceRate;
        this.averageDaysToHire = averageDaysToHire;
        this.candidatesByStage = candidatesByStage;
        this.requisitionsByDepartment = requisitionsByDepartment;
        this.priorityRequisitions = priorityRequisitions;
        this.recentApplicants = recentApplicants;
    }

    public int getOpenRequisitions() { return openRequisitions; }
    public void setOpenRequisitions(int openRequisitions) { this.openRequisitions = openRequisitions; }

    public int getTotalCandidatesInPipeline() { return totalCandidatesInPipeline; }
    public void setTotalCandidatesInPipeline(int totalCandidatesInPipeline) { this.totalCandidatesInPipeline = totalCandidatesInPipeline; }

    public int getActiveInterviews() { return activeInterviews; }
    public void setActiveInterviews(int activeInterviews) { this.activeInterviews = activeInterviews; }

    public int getOffersExtended() { return offersExtended; }
    public void setOffersExtended(int offersExtended) { this.offersExtended = offersExtended; }

    public int getHiredThisQuarter() { return hiredThisQuarter; }
    public void setHiredThisQuarter(int hiredThisQuarter) { this.hiredThisQuarter = hiredThisQuarter; }

    public double getOfferAcceptanceRate() { return offerAcceptanceRate; }
    public void setOfferAcceptanceRate(double offerAcceptanceRate) { this.offerAcceptanceRate = offerAcceptanceRate; }

    public int getAverageDaysToHire() { return averageDaysToHire; }
    public void setAverageDaysToHire(int averageDaysToHire) { this.averageDaysToHire = averageDaysToHire; }

    public Map<String, Integer> getCandidatesByStage() { return candidatesByStage; }
    public void setCandidatesByStage(Map<String, Integer> candidatesByStage) { this.candidatesByStage = candidatesByStage; }

    public Map<String, Integer> getRequisitionsByDepartment() { return requisitionsByDepartment; }
    public void setRequisitionsByDepartment(Map<String, Integer> requisitionsByDepartment) { this.requisitionsByDepartment = requisitionsByDepartment; }

    public List<JobRequisitionDto> getPriorityRequisitions() { return priorityRequisitions; }
    public void setPriorityRequisitions(List<JobRequisitionDto> priorityRequisitions) { this.priorityRequisitions = priorityRequisitions; }

    public List<CandidateDto> getRecentApplicants() { return recentApplicants; }
    public void setRecentApplicants(List<CandidateDto> recentApplicants) { this.recentApplicants = recentApplicants; }
}
