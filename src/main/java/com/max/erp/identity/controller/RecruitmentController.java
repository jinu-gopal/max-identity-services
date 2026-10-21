package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.RecruitmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruitment")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    @GetMapping("/summary")
    public ResponseEntity<RecruitmentSummaryDto> getSummary() {
        return ResponseEntity.ok(recruitmentService.getRecruitmentSummary());
    }

    @GetMapping("/requisitions")
    public ResponseEntity<List<JobRequisitionDto>> getRequisitions(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(recruitmentService.getAllRequisitions(department, status));
    }

    @GetMapping("/requisitions/{id}")
    public ResponseEntity<JobRequisitionDto> getRequisitionById(@PathVariable String id) {
        JobRequisitionDto req = recruitmentService.getRequisitionById(id);
        if (req == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(req);
    }

    @PostMapping("/requisitions")
    public ResponseEntity<JobRequisitionDto> createRequisition(@Valid @RequestBody CreateJobRequisitionRequest request) {
        JobRequisitionDto created = recruitmentService.createRequisition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/requisitions/{id}/approval")
    public ResponseEntity<JobRequisitionDto> updateRequisitionApproval(
            @PathVariable String id,
            @RequestBody java.util.Map<String, Object> body) {
        int step = body.containsKey("step") ? ((Number) body.get("step")).intValue() : 1;
        String action = (String) body.getOrDefault("action", "APPROVE");
        String approverName = (String) body.getOrDefault("approverName", "Authorized Approver");

        JobRequisitionDto updated = recruitmentService.updateRequisitionApproval(id, step, action, approverName);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateDto>> getCandidates(
            @RequestParam(required = false) String requisitionId,
            @RequestParam(required = false) String stage) {
        return ResponseEntity.ok(recruitmentService.getAllCandidates(requisitionId, stage));
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<CandidateDto> getCandidateById(@PathVariable String id) {
        CandidateDto cand = recruitmentService.getCandidateById(id);
        if (cand == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cand);
    }

    @PostMapping("/candidates")
    public ResponseEntity<CandidateDto> createCandidate(@Valid @RequestBody CreateCandidateRequest request) {
        CandidateDto created = recruitmentService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/candidates/{id}/stage")
    public ResponseEntity<CandidateDto> updateCandidateStage(
            @PathVariable String id,
            @Valid @RequestBody UpdateCandidateStageRequest request) {
        CandidateDto updated = recruitmentService.updateCandidateStage(id, request);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/scorecards")
    public ResponseEntity<List<InterviewScorecardDto>> getScorecards(
            @RequestParam(required = false) String candidateId) {
        return ResponseEntity.ok(recruitmentService.getAllScorecards(candidateId));
    }

    @PostMapping("/scorecards")
    public ResponseEntity<InterviewScorecardDto> submitScorecard(@Valid @RequestBody CreateScorecardRequest request) {
        InterviewScorecardDto created = recruitmentService.submitScorecard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/public/requisitions")
    public ResponseEntity<List<JobRequisitionDto>> getPublicRequisitions() {
        return ResponseEntity.ok(recruitmentService.getAllRequisitions(null, "OPEN"));
    }

    @PostMapping("/public/apply")
    public ResponseEntity<CandidateDto> publicApply(@Valid @RequestBody CreateCandidateRequest request) {
        request.setSource("CAREERS_PORTAL");
        CandidateDto created = recruitmentService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/candidates/{id}/hire-to-workforce")
    public ResponseEntity<EmployeeDto> hireCandidateToWorkforce(@PathVariable String id) {
        EmployeeDto emp = recruitmentService.hireCandidateToWorkforce(id);
        if (emp == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(emp);
    }
}
