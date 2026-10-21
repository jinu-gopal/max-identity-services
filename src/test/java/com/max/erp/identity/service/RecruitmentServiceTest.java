package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecruitmentServiceTest {

    private RecruitmentService recruitmentService;

    @BeforeEach
    void setUp() {
        recruitmentService = new RecruitmentService();
    }

    @Test
    void testRecruitmentSummary() {
        RecruitmentSummaryDto summary = recruitmentService.getRecruitmentSummary();
        assertNotNull(summary);
        assertTrue(summary.getOpenRequisitions() >= 2);
        assertTrue(summary.getTotalCandidatesInPipeline() >= 8);
        assertTrue(summary.getActiveInterviews() >= 2);
        assertTrue(summary.getOffersExtended() >= 1);
        assertTrue(summary.getOfferAcceptanceRate() > 80.0);
        assertTrue(summary.getAverageDaysToHire() > 0);
        assertNotNull(summary.getCandidatesByStage());
        assertTrue(summary.getCandidatesByStage().containsKey("INTERVIEW"));
        assertFalse(summary.getPriorityRequisitions().isEmpty());
    }

    @Test
    void testGetAllRequisitionsAndFilter() {
        List<JobRequisitionDto> all = recruitmentService.getAllRequisitions(null, null);
        assertNotNull(all);
        assertTrue(all.size() >= 4);

        List<JobRequisitionDto> eng = recruitmentService.getAllRequisitions("Engineering", null);
        assertNotNull(eng);
        assertFalse(eng.isEmpty());
        assertTrue(eng.stream().allMatch(r -> "Engineering".equalsIgnoreCase(r.getDepartment())));

        JobRequisitionDto first = all.get(0);
        JobRequisitionDto found = recruitmentService.getRequisitionById(first.getId());
        assertNotNull(found);
        assertEquals(first.getTitle(), found.getTitle());
    }

    @Test
    void testCreateJobRequisition() {
        CreateJobRequisitionRequest req = new CreateJobRequisitionRequest();
        req.setTitle("Staff DevOps Infrastructure Engineer");
        req.setDepartment("Engineering");
        req.setJobLevel("SENIOR");
        req.setEmploymentType("FULL_TIME");
        req.setLocation("REMOTE");
        req.setTargetHeadcount(2);
        req.setSalaryRange("$160,000 - $190,000");
        req.setHiringManagerName("Marcus Chen");
        req.setRequiredSkills(List.of("Kubernetes", "Terraform", "AWS", "CI/CD"));

        JobRequisitionDto created = recruitmentService.createRequisition(req);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Staff DevOps Infrastructure Engineer", created.getTitle());
        assertEquals("OPEN", created.getStatus());
        assertEquals(2, created.getTargetHeadcount());
        assertEquals(0, created.getFilledHeadcount());

        JobRequisitionDto found = recruitmentService.getRequisitionById(created.getId());
        assertNotNull(found);
        assertEquals("Staff DevOps Infrastructure Engineer", found.getTitle());
    }

    @Test
    void testCandidateLifecycleAndHireIncrement() {
        CreateCandidateRequest candReq = new CreateCandidateRequest();
        candReq.setFullName("Aiden Cross");
        candReq.setEmail("aiden.cross@kernel-sec.io");
        candReq.setPhone("+1 (415) 555-1299");
        candReq.setRequisitionId("req-102"); // req-102 has targetHeadcount: 1, filledHeadcount: 0
        candReq.setSource("REFERRAL");
        candReq.setResumeSummary("10 years manufacturing SCADA cybersecurity lead.");

        CandidateDto candidate = recruitmentService.createCandidate(candReq);
        assertNotNull(candidate);
        assertEquals("APPLIED", candidate.getStage());

        // Advance to INTERVIEW
        UpdateCandidateStageRequest stageReq = new UpdateCandidateStageRequest();
        stageReq.setStage("INTERVIEW");
        stageReq.setNotes("Passed screening interview with flying colors.");

        CandidateDto updatedStage = recruitmentService.updateCandidateStage(candidate.getId(), stageReq);
        assertNotNull(updatedStage);
        assertEquals("INTERVIEW", updatedStage.getStage());

        // Submit Scorecard
        CreateScorecardRequest scReq = new CreateScorecardRequest();
        scReq.setCandidateId(candidate.getId());
        scReq.setInterviewerName("Carlos Gomez");
        scReq.setRoundType("TECHNICAL");
        scReq.setScore(5);
        scReq.setRecommendation("STRONG_HIRE");
        scReq.setFeedbackSummary("Unmatched expertise in industrial control protocols.");

        InterviewScorecardDto sc = recruitmentService.submitScorecard(scReq);
        assertNotNull(sc);
        assertEquals(5, sc.getScore());

        CandidateDto scoredCandidate = recruitmentService.getCandidateById(candidate.getId());
        assertEquals(5.0, scoredCandidate.getInterviewScore());

        // Advance to HIRED
        UpdateCandidateStageRequest hireReq = new UpdateCandidateStageRequest();
        hireReq.setStage("HIRED");
        hireReq.setNotes("Candidate accepted offer. Onboarding next Monday.");

        CandidateDto hired = recruitmentService.updateCandidateStage(candidate.getId(), hireReq);
        assertNotNull(hired);
        assertEquals("HIRED", hired.getStage());

        JobRequisitionDto reqAfter = recruitmentService.getRequisitionById("req-102");
        assertEquals(1, reqAfter.getFilledHeadcount());
        assertEquals("FILLED", reqAfter.getStatus());
    }

    @Test
    void testHireCandidateToWorkforce() {
        EmployeeDto emp = recruitmentService.hireCandidateToWorkforce("cand-203"); // Emily Zhang
        assertNotNull(emp);
        assertNotNull(emp.getId());
        assertEquals("Emily Zhang", emp.getFullName());
        assertEquals("Engineering", emp.getDepartment());
        assertEquals("ACTIVE", emp.getStatus());

        CandidateDto candAfter = recruitmentService.getCandidateById("cand-203");
        assertEquals("HIRED", candAfter.getStage());
    }
}
