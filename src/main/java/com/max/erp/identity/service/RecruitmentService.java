package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecruitmentService {

    private static final Logger log = LoggerFactory.getLogger(RecruitmentService.class);

    private final Map<String, JobRequisitionDto> requisitionsMap = new ConcurrentHashMap<>();
    private final Map<String, CandidateDto> candidatesMap = new ConcurrentHashMap<>();
    private final Map<String, InterviewScorecardDto> scorecardsMap = new ConcurrentHashMap<>();
    private final WorkforceService workforceService;

    public RecruitmentService() {
        this(new WorkforceService());
    }

    public RecruitmentService(WorkforceService workforceService) {
        this.workforceService = workforceService;
        initializeRequisitions();
        initializeCandidates();
        initializeScorecards();
    }

    private void initializeRequisitions() {
        JobRequisitionDto req1 = new JobRequisitionDto(
                "req-101", "REQ-ENG-084", "Senior Full-Stack Cloud Architect", "Engineering",
                "SENIOR", "FULL_TIME", "REMOTE", 2, 0, "$150,000 - $185,000",
                "Marcus Chen", "OPEN", List.of("Java 21", "Next.js", "Spring Boot", "Distributed Systems"),
                Instant.now().minusSeconds(1200000)
        );

        JobRequisitionDto req2 = new JobRequisitionDto(
                "req-102", "REQ-OPS-032", "Plant Reliability & Safety Lead", "Operations",
                "LEAD", "FULL_TIME", "HEADQUARTERS", 1, 0, "$125,000 - $155,000",
                "Carlos Gomez", "INTERVIEWING", List.of("SCADA Systems", "OSHA Compliance", "Lean Six Sigma"),
                Instant.now().minusSeconds(1800000)
        );

        JobRequisitionDto req3 = new JobRequisitionDto(
                "req-103", "REQ-FIN-019", "Corporate Financial Planning Analyst", "Finance",
                "MID", "FULL_TIME", "HEADQUARTERS", 1, 1, "$110,000 - $135,000",
                "Sarah Jenkins", "FILLED", List.of("Financial Modeling", "US GAAP", "ERP General Ledger"),
                Instant.now().minusSeconds(2500000)
        );

        JobRequisitionDto req4 = new JobRequisitionDto(
                "req-104", "REQ-SLS-055", "Enterprise Global Solutions Executive", "Sales",
                "SENIOR", "FULL_TIME", "REMOTE", 3, 1, "$130,000 - $165,000 + Comm",
                "Olivia Taylor", "OPEN", List.of("B2B SaaS Sales", "Contract Negotiation", "MEDDPICC"),
                Instant.now().minusSeconds(900000)
        );

        List.of(req1, req2, req3, req4).forEach(r -> requisitionsMap.put(r.getId(), r));
    }

    private void initializeCandidates() {
        CandidateDto c1 = new CandidateDto(
                "cand-201", "Maya Patel", "maya.patel@devtalent.net", "+1 (415) 555-8912",
                "req-101", "Senior Full-Stack Cloud Architect", "Engineering",
                "INTERVIEW", "LINKEDIN", "8 years distributed cloud systems experience. Ex-Stripe infrastructure lead.",
                "https://github.com/mayapatel-cloud", 4.8, "Exceptional systems design answers; strong concurrency grasp.",
                Instant.now().minusSeconds(600000), Instant.now().minusSeconds(86400)
        );

        CandidateDto c2 = new CandidateDto(
                "cand-202", "Lucas Schneider", "lucas.schneider@berlin-tech.io", "+49 30 555 0192",
                "req-101", "Senior Full-Stack Cloud Architect", "Engineering",
                "SCREENING", "REFERRAL", "6 years Java & modern TypeScript engineering. Solid algorithmic foundations.",
                "https://linkedin.com/in/lucas-schneider", 3.8, "HR phone screen passed. Scheduled technical coding loop.",
                Instant.now().minusSeconds(400000), Instant.now().minusSeconds(43200)
        );

        CandidateDto c3 = new CandidateDto(
                "cand-203", "Emily Zhang", "emily.zhang@mit.alumni.edu", "+1 (617) 555-2384",
                "req-101", "Senior Full-Stack Cloud Architect", "Engineering",
                "OFFER", "CAREERS_PORTAL", "10 years principal engineer experience. Author of high-throughput messaging framework.",
                "https://emilyzhang.dev", 4.9, "Formal offer packet extended. Sign-on bonus and equity finalized.",
                Instant.now().minusSeconds(1000000), Instant.now().minusSeconds(14400)
        );

        CandidateDto c4 = new CandidateDto(
                "cand-204", "Daniel O'Connor", "daniel.oc@industrial-ops.com", "+1 (312) 555-9011",
                "req-102", "Plant Reliability & Safety Lead", "Operations",
                "APPLIED", "CAREERS_PORTAL", "Plant supervisor at heavy manufacturing facility with zero recordable incidents in 3 yrs.",
                null, 0.0, "New application received today via careers site.",
                Instant.now().minusSeconds(28800), Instant.now().minusSeconds(28800)
        );

        CandidateDto c5 = new CandidateDto(
                "cand-205", "Samuel Kim", "sam.kim.safety@koreaplant.org", "+82 2 555 7712",
                "req-102", "Plant Reliability & Safety Lead", "Operations",
                "INTERVIEW", "HEADHUNTER", "12 years multi-site plant reliability and predictive equipment maintenance lead.",
                null, 4.2, "Onsite plant simulation and safety protocol panel completed.",
                Instant.now().minusSeconds(800000), Instant.now().minusSeconds(72000)
        );

        CandidateDto c6 = new CandidateDto(
                "cand-206", "Chloe Bennett", "chloe.bennett@cpa-finance.org", "+1 (212) 555-4491",
                "req-103", "Corporate Financial Planning Analyst", "Finance",
                "HIRED", "REFERRAL", "Big 4 audit senior. Certified Public Accountant with multi-entity consolidation proficiency.",
                null, 4.7, "Offer accepted! Start date set for next month. Transitioned to Workforce Studio.",
                Instant.now().minusSeconds(2000000), Instant.now().minusSeconds(864000)
        );

        CandidateDto c7 = new CandidateDto(
                "cand-207", "Liam Vance", "liam.vance@enterprisesales.co", "+1 (303) 555-6671",
                "req-104", "Enterprise Global Solutions Executive", "Sales",
                "SCREENING", "LINKEDIN", "Consistent $3M+ quota achiever across Fortune 500 accounts.",
                null, 3.5, "Initial screening call complete with Sales VP Olivia Taylor.",
                Instant.now().minusSeconds(350000), Instant.now().minusSeconds(120000)
        );

        CandidateDto c8 = new CandidateDto(
                "cand-208", "Sophia Rodriguez", "sophia.r@outboundreach.io", "+1 (512) 555-3211",
                "req-104", "Enterprise Global Solutions Executive", "Sales",
                "REJECTED", "CAREERS_PORTAL", "Sales background primarily transactional retail rather than enterprise B2B complex deals.",
                null, 2.1, "Polite rejection notification sent with talent pool retention.",
                Instant.now().minusSeconds(700000), Instant.now().minusSeconds(400000)
        );

        List.of(c1, c2, c3, c4, c5, c6, c7, c8).forEach(c -> candidatesMap.put(c.getId(), c));
    }

    private void initializeScorecards() {
        InterviewScorecardDto sc1 = new InterviewScorecardDto(
                "sc-301", "cand-201", "Maya Patel", "req-101",
                "Priya Sharma", "TECHNICAL", 5, "STRONG_HIRE",
                "Deep knowledge of reactive concurrency and database indexing. Implemented problem cleanly in 30 minutes.",
                Instant.now().minusSeconds(172800)
        );

        InterviewScorecardDto sc2 = new InterviewScorecardDto(
                "sc-302", "cand-201", "Maya Patel", "req-101",
                "Marcus Chen", "SYSTEM_DESIGN", 4, "HIRE",
                "Solid architectural trade-off reasoning regarding eventual consistency vs ACID in multi-tenant ERP context.",
                Instant.now().minusSeconds(86400)
        );

        InterviewScorecardDto sc3 = new InterviewScorecardDto(
                "sc-303", "cand-203", "Emily Zhang", "req-101",
                "Marcus Chen", "EXECUTIVE", 5, "STRONG_HIRE",
                "Tremendous engineering vision and demonstrated leadership in scaled microservice architecture.",
                Instant.now().minusSeconds(200000)
        );

        InterviewScorecardDto sc4 = new InterviewScorecardDto(
                "sc-304", "cand-205", "Samuel Kim", "req-102",
                "Carlos Gomez", "TECHNICAL", 4, "HIRE",
                "Impressive depth in root cause analysis (RCA) and emergency response protocols.",
                Instant.now().minusSeconds(100000)
        );

        List.of(sc1, sc2, sc3, sc4).forEach(sc -> scorecardsMap.put(sc.getId(), sc));
    }

    public RecruitmentSummaryDto getRecruitmentSummary() {
        int openReqs = (int) requisitionsMap.values().stream()
                .filter(r -> "OPEN".equalsIgnoreCase(r.getStatus()) || "INTERVIEWING".equalsIgnoreCase(r.getStatus()))
                .count();

        int totalCandidates = candidatesMap.size();

        int activeInterviews = (int) candidatesMap.values().stream()
                .filter(c -> "INTERVIEW".equalsIgnoreCase(c.getStage()))
                .count();

        int offers = (int) candidatesMap.values().stream()
                .filter(c -> "OFFER".equalsIgnoreCase(c.getStage()))
                .count();

        int hired = (int) candidatesMap.values().stream()
                .filter(c -> "HIRED".equalsIgnoreCase(c.getStage()))
                .count();

        Map<String, Integer> stageCounts = candidatesMap.values().stream()
                .collect(Collectors.groupingBy(CandidateDto::getStage, Collectors.summingInt(c -> 1)));

        Map<String, Integer> reqDepts = requisitionsMap.values().stream()
                .collect(Collectors.groupingBy(JobRequisitionDto::getDepartment, Collectors.summingInt(r -> 1)));

        List<JobRequisitionDto> priority = requisitionsMap.values().stream()
                .filter(r -> !"FILLED".equalsIgnoreCase(r.getStatus()))
                .sorted(Comparator.comparing(JobRequisitionDto::getCreatedAt).reversed())
                .collect(Collectors.toList());

        List<CandidateDto> recent = candidatesMap.values().stream()
                .sorted(Comparator.comparing(CandidateDto::getLastActivityAt).reversed())
                .limit(6)
                .collect(Collectors.toList());

        return new RecruitmentSummaryDto(
                openReqs, totalCandidates, activeInterviews, offers, hired,
                88.5, 21, stageCounts, reqDepts, priority, recent
        );
    }

    public List<JobRequisitionDto> getAllRequisitions(String department, String status) {
        return requisitionsMap.values().stream()
                .filter(r -> department == null || department.isBlank() || department.equalsIgnoreCase(r.getDepartment()))
                .filter(r -> status == null || status.isBlank() || status.equalsIgnoreCase(r.getStatus()))
                .sorted(Comparator.comparing(JobRequisitionDto::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public JobRequisitionDto getRequisitionById(String id) {
        return requisitionsMap.get(id);
    }

    public JobRequisitionDto createRequisition(CreateJobRequisitionRequest req) {
        String id = "req-" + UUID.randomUUID().toString().substring(0, 8);
        String reqNum = "REQ-" + req.getDepartment().substring(0, Math.min(3, req.getDepartment().length())).toUpperCase() + "-" + (100 + requisitionsMap.size() + 1);

        String initialStatus = "OPEN";
        String approvalStatus = "APPROVED";
        int approvalStep = 3;
        if ("PENDING_APPROVAL".equalsIgnoreCase(req.getApprovalStatus()) || "DRAFT".equalsIgnoreCase(req.getApprovalStatus())) {
            initialStatus = req.getApprovalStatus().toUpperCase();
            approvalStatus = req.getApprovalStatus().toUpperCase();
            approvalStep = "DRAFT".equalsIgnoreCase(req.getApprovalStatus()) ? 0 : 1;
        }

        JobRequisitionDto dto = new JobRequisitionDto(
                id,
                reqNum,
                req.getTitle(),
                req.getDepartment(),
                req.getJobLevel() != null ? req.getJobLevel() : "MID",
                req.getEmploymentType() != null ? req.getEmploymentType() : "FULL_TIME",
                req.getLocation() != null ? req.getLocation() : "HEADQUARTERS",
                req.getTargetHeadcount() > 0 ? req.getTargetHeadcount() : 1,
                0,
                req.getSalaryRange() != null ? req.getSalaryRange() : "$120,000 - $150,000",
                req.getHiringManagerName() != null ? req.getHiringManagerName() : "Department Head",
                initialStatus,
                req.getRequiredSkills() != null ? req.getRequiredSkills() : new ArrayList<>(),
                Instant.now()
        );

        if (req.getOrgUnitId() != null) dto.setOrgUnitId(req.getOrgUnitId());
        if (req.getOrgUnitName() != null) dto.setOrgUnitName(req.getOrgUnitName());
        if (req.getPositionId() != null) dto.setPositionId(req.getPositionId());
        dto.setApprovalStatus(approvalStatus);
        dto.setCurrentApprovalStep(approvalStep);

        requisitionsMap.put(id, dto);
        log.info("Created Job Requisition: {} ({}) OrgUnit: {} Pos: {}", dto.getTitle(), dto.getId(), dto.getOrgUnitName(), dto.getPositionId());
        return dto;
    }

    public JobRequisitionDto updateRequisitionApproval(String id, int step, String action, String approverName) {
        JobRequisitionDto req = requisitionsMap.get(id);
        if (req == null) return null;

        if ("REJECT".equalsIgnoreCase(action)) {
            req.setApprovalStatus("REJECTED");
            req.setStatus("REJECTED");
        } else if ("APPROVE".equalsIgnoreCase(action)) {
            if (step >= 3) {
                req.setApprovalStatus("APPROVED");
                req.setStatus("OPEN");
                req.setCurrentApprovalStep(3);
            } else {
                req.setCurrentApprovalStep(step + 1);
            }
        }
        return req;
    }

    public List<CandidateDto> getAllCandidates(String requisitionId, String stage) {
        return candidatesMap.values().stream()
                .filter(c -> requisitionId == null || requisitionId.isBlank() || requisitionId.equalsIgnoreCase(c.getRequisitionId()))
                .filter(c -> stage == null || stage.isBlank() || stage.equalsIgnoreCase(c.getStage()))
                .sorted(Comparator.comparing(CandidateDto::getLastActivityAt).reversed())
                .collect(Collectors.toList());
    }

    public CandidateDto getCandidateById(String id) {
        return candidatesMap.get(id);
    }

    public CandidateDto createCandidate(CreateCandidateRequest req) {
        String id = "cand-" + UUID.randomUUID().toString().substring(0, 8);
        JobRequisitionDto requisition = requisitionsMap.get(req.getRequisitionId());

        CandidateDto dto = new CandidateDto(
                id,
                req.getFullName(),
                req.getEmail(),
                req.getPhone() != null ? req.getPhone() : "+1 (555) 000-0000",
                req.getRequisitionId(),
                requisition != null ? requisition.getTitle() : "General Application",
                requisition != null ? requisition.getDepartment() : "General",
                "APPLIED",
                req.getSource() != null ? req.getSource() : "CAREERS_PORTAL",
                req.getResumeSummary() != null ? req.getResumeSummary() : "Direct application submitted.",
                req.getPortfolioUrl(),
                0.0,
                req.getNotes(),
                Instant.now(),
                Instant.now()
        );

        candidatesMap.put(id, dto);
        log.info("Registered Candidate Applicant: {} for {}", dto.getFullName(), dto.getRequisitionTitle());
        return dto;
    }

    public CandidateDto updateCandidateStage(String id, UpdateCandidateStageRequest req) {
        CandidateDto existing = candidatesMap.get(id);
        if (existing == null) return null;

        existing.setStage(req.getStage().toUpperCase());
        if (req.getNotes() != null && !req.getNotes().isBlank()) {
            existing.setNotes(req.getNotes());
        }
        existing.setLastActivityAt(Instant.now());

        if ("HIRED".equalsIgnoreCase(req.getStage())) {
            JobRequisitionDto r = requisitionsMap.get(existing.getRequisitionId());
            if (r != null) {
                r.setFilledHeadcount(r.getFilledHeadcount() + 1);
                if (r.getFilledHeadcount() >= r.getTargetHeadcount()) {
                    r.setStatus("FILLED");
                }
            }
        }

        log.info("Advanced candidate {} to stage {}", existing.getFullName(), req.getStage());
        return existing;
    }

    public List<InterviewScorecardDto> getAllScorecards(String candidateId) {
        return scorecardsMap.values().stream()
                .filter(s -> candidateId == null || candidateId.isBlank() || candidateId.equalsIgnoreCase(s.getCandidateId()))
                .sorted(Comparator.comparing(InterviewScorecardDto::getConductedAt).reversed())
                .collect(Collectors.toList());
    }

    public InterviewScorecardDto submitScorecard(CreateScorecardRequest req) {
        String id = "sc-" + UUID.randomUUID().toString().substring(0, 8);
        CandidateDto candidate = candidatesMap.get(req.getCandidateId());

        InterviewScorecardDto dto = new InterviewScorecardDto(
                id,
                req.getCandidateId(),
                candidate != null ? candidate.getFullName() : "Candidate",
                candidate != null ? candidate.getRequisitionId() : "general",
                req.getInterviewerName(),
                req.getRoundType(),
                req.getScore(),
                req.getRecommendation(),
                req.getFeedbackSummary(),
                Instant.now()
        );

        scorecardsMap.put(id, dto);

        // Update candidate average score
        if (candidate != null) {
            List<InterviewScorecardDto> candidateCards = getAllScorecards(candidate.getId());
            double avg = candidateCards.stream().mapToInt(InterviewScorecardDto::getScore).average().orElse(req.getScore());
            candidate.setInterviewScore(Math.round(avg * 10.0) / 10.0);
            candidate.setLastActivityAt(Instant.now());
        }

        log.info("Submitted interview scorecard for candidate: {}", dto.getCandidateName());
        return dto;
    }

    public EmployeeDto hireCandidateToWorkforce(String candidateId) {
        CandidateDto candidate = candidatesMap.get(candidateId);
        if (candidate == null) return null;

        UpdateCandidateStageRequest stageReq = new UpdateCandidateStageRequest();
        stageReq.setStage("HIRED");
        stageReq.setNotes("Hired through recruitment pipeline and converted to active workforce employee.");
        updateCandidateStage(candidateId, stageReq);

        String[] parts = candidate.getFullName().trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "Staff";

        CreateEmployeeRequest empReq = new CreateEmployeeRequest();
        empReq.setFirstName(firstName);
        empReq.setLastName(lastName);
        empReq.setEmail(candidate.getEmail());
        empReq.setPhone(candidate.getPhone());
        empReq.setDepartment(candidate.getDepartment() != null ? candidate.getDepartment() : "General");
        empReq.setJobTitle(candidate.getRequisitionTitle() != null ? candidate.getRequisitionTitle() : "Specialist");
        empReq.setEmploymentType("FULL_TIME");
        empReq.setWorkLocation("HEADQUARTERS");
        empReq.setRemainingPtoDays(20);

        EmployeeDto employee = workforceService.createEmployee(empReq);
        log.info("Successfully converted candidate {} into workforce employee {} ({})",
                candidate.getFullName(), employee.getFullName(), employee.getId());
        return employee;
    }
}
