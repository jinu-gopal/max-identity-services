package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WorkforceService {

    private static final Logger log = LoggerFactory.getLogger(WorkforceService.class);

    private final Map<String, EmployeeDto> employeesMap = new ConcurrentHashMap<>();
    private final Map<String, ShiftAssignmentDto> shiftsMap = new ConcurrentHashMap<>();
    private final Map<String, LeaveRequestDto> leavesMap = new ConcurrentHashMap<>();

    public WorkforceService() {
        initializeEmployees();
        initializeShifts();
        initializeLeaves();
    }

    private void initializeEmployees() {
        EmployeeDto ceo = new EmployeeDto(
                "emp-001", "MX-1001", "Elena", "Vance",
                "elena.vance@max-erp.global", "+1 (415) 555-0101", "Executive", "Chief Executive Officer",
                null, null, "FULL_TIME", "HEADQUARTERS", "ACTIVE", "2021-03-15", 25, Instant.now().minusSeconds(86400000)
        );

        EmployeeDto cto = new EmployeeDto(
                "emp-002", "MX-1002", "Marcus", "Chen",
                "marcus.chen@max-erp.global", "+1 (415) 555-0102", "Engineering", "Chief Technology Officer",
                "emp-001", "Elena Vance", "FULL_TIME", "HEADQUARTERS", "ACTIVE", "2021-06-01", 18, Instant.now().minusSeconds(80000000)
        );

        EmployeeDto cfo = new EmployeeDto(
                "emp-003", "MX-1003", "Sarah", "Jenkins",
                "sarah.jenkins@max-erp.global", "+1 (415) 555-0103", "Finance", "Chief Financial Officer",
                "emp-001", "Elena Vance", "FULL_TIME", "HEADQUARTERS", "ACTIVE", "2021-08-15", 22, Instant.now().minusSeconds(75000000)
        );

        EmployeeDto coo = new EmployeeDto(
                "emp-004", "MX-1004", "David", "Ross",
                "david.ross@max-erp.global", "+1 (415) 555-0104", "Operations", "Chief Operating Officer",
                "emp-001", "Elena Vance", "FULL_TIME", "HEADQUARTERS", "ACTIVE", "2022-01-10", 16, Instant.now().minusSeconds(70000000)
        );

        EmployeeDto engLead = new EmployeeDto(
                "emp-005", "MX-1005", "Priya", "Sharma",
                "priya.sharma@max-erp.global", "+1 (415) 555-0105", "Engineering", "Lead Systems Architect",
                "emp-002", "Marcus Chen", "FULL_TIME", "REMOTE", "ACTIVE", "2022-04-18", 14, Instant.now().minusSeconds(60000000)
        );

        EmployeeDto engDev = new EmployeeDto(
                "emp-006", "MX-1006", "Alex", "Morgan",
                "alex.morgan@max-erp.global", "+1 (415) 555-0106", "Engineering", "Senior Full-Stack Engineer",
                "emp-005", "Priya Sharma", "FULL_TIME", "REMOTE", "ACTIVE", "2023-02-01", 12, Instant.now().minusSeconds(40000000)
        );

        EmployeeDto opsMgr = new EmployeeDto(
                "emp-007", "MX-1007", "Carlos", "Gomez",
                "carlos.gomez@max-erp.global", "+1 (415) 555-0107", "Operations", "Plant Operations Manager",
                "emp-004", "David Ross", "FULL_TIME", "HEADQUARTERS", "ACTIVE", "2022-09-01", 15, Instant.now().minusSeconds(50000000)
        );

        EmployeeDto opsLead = new EmployeeDto(
                "emp-008", "MX-1008", "Rachel", "Green",
                "rachel.green@max-erp.global", "+1 (415) 555-0108", "Operations", "Lead Shift Supervisor",
                "emp-007", "Carlos Gomez", "FULL_TIME", "HEADQUARTERS", "ON_LEAVE", "2023-05-15", 8, Instant.now().minusSeconds(30000000)
        );

        EmployeeDto finMgr = new EmployeeDto(
                "emp-009", "MX-1009", "James", "Wilson",
                "james.wilson@max-erp.global", "+1 (415) 555-0109", "Finance", "Accounting & Controller Lead",
                "emp-003", "Sarah Jenkins", "FULL_TIME", "HEADQUARTERS", "ACTIVE", "2022-11-20", 19, Instant.now().minusSeconds(45000000)
        );

        EmployeeDto salesDir = new EmployeeDto(
                "emp-010", "MX-1010", "Olivia", "Taylor",
                "olivia.taylor@max-erp.global", "+1 (415) 555-0110", "Sales", "Global Enterprise Sales Director",
                "emp-001", "Elena Vance", "FULL_TIME", "REMOTE", "ACTIVE", "2023-01-15", 21, Instant.now().minusSeconds(42000000)
        );

        List.of(ceo, cto, cfo, coo, engLead, engDev, opsMgr, opsLead, finMgr, salesDir)
                .forEach(e -> employeesMap.put(e.getId(), e));
    }

    private void initializeShifts() {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();

        ShiftAssignmentDto s1 = new ShiftAssignmentDto(
                "shift-101", "emp-004", "David Ross", "Operations",
                today, "MORNING", "08:00", "16:30", 8.0, "HEADQUARTERS", "CLOCKED_IN", Instant.now().minusSeconds(14400)
        );

        ShiftAssignmentDto s2 = new ShiftAssignmentDto(
                "shift-102", "emp-007", "Carlos Gomez", "Operations",
                today, "MORNING", "08:00", "16:30", 8.0, "HEADQUARTERS", "CLOCKED_IN", Instant.now().minusSeconds(14400)
        );

        ShiftAssignmentDto s3 = new ShiftAssignmentDto(
                "shift-103", "emp-005", "Priya Sharma", "Engineering",
                today, "MORNING", "09:00", "17:30", 8.0, "REMOTE", "SCHEDULED", Instant.now().minusSeconds(7200)
        );

        ShiftAssignmentDto s4 = new ShiftAssignmentDto(
                "shift-104", "emp-006", "Alex Morgan", "Engineering",
                today, "EVENING", "16:00", "00:00", 8.0, "REMOTE", "SCHEDULED", Instant.now().minusSeconds(3600)
        );

        ShiftAssignmentDto s5 = new ShiftAssignmentDto(
                "shift-105", "emp-007", "Carlos Gomez", "Operations",
                tomorrow, "MORNING", "08:00", "16:30", 8.0, "HEADQUARTERS", "SCHEDULED", Instant.now().minusSeconds(1800)
        );

        List.of(s1, s2, s3, s4, s5).forEach(s -> shiftsMap.put(s.getId(), s));
    }

    private void initializeLeaves() {
        LeaveRequestDto l1 = new LeaveRequestDto(
                "leave-201", "emp-008", "Rachel Green", "Operations",
                "VACATION", LocalDate.now().minusDays(1).toString(), LocalDate.now().plusDays(4).toString(), 5,
                "Annual family holiday in Hawaii", "APPROVED", "David Ross", "Approved, coverage handled by Carlos Gomez",
                Instant.now().minusSeconds(259200), Instant.now().minusSeconds(172800)
        );

        LeaveRequestDto l2 = new LeaveRequestDto(
                "leave-202", "emp-006", "Alex Morgan", "Engineering",
                "SICK", LocalDate.now().plusDays(3).toString(), LocalDate.now().plusDays(4).toString(), 2,
                "Dental surgery recovery", "PENDING", null, null,
                Instant.now().minusSeconds(43200), null
        );

        LeaveRequestDto l3 = new LeaveRequestDto(
                "leave-203", "emp-009", "James Wilson", "Finance",
                "VACATION", LocalDate.now().plusDays(10).toString(), LocalDate.now().plusDays(14).toString(), 4,
                "Personal time off following fiscal Q3 closing", "PENDING", null, null,
                Instant.now().minusSeconds(86400), null
        );

        List.of(l1, l2, l3).forEach(l -> leavesMap.put(l.getId(), l));
    }

    public WorkforceSummaryDto getWorkforceSummary() {
        int total = employeesMap.size();
        int active = (int) employeesMap.values().stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();
        int onLeave = (int) employeesMap.values().stream().filter(e -> "ON_LEAVE".equalsIgnoreCase(e.getStatus())).count();

        String today = LocalDate.now().toString();
        List<ShiftAssignmentDto> todaysShifts = shiftsMap.values().stream()
                .filter(s -> today.equals(s.getShiftDate()))
                .sorted(Comparator.comparing(ShiftAssignmentDto::getStartTime))
                .collect(Collectors.toList());

        int onDuty = (int) todaysShifts.stream()
                .filter(s -> "CLOCKED_IN".equalsIgnoreCase(s.getStatus()) || "SCHEDULED".equalsIgnoreCase(s.getStatus()))
                .count();

        int pendingLeaves = (int) leavesMap.values().stream()
                .filter(l -> "PENDING".equalsIgnoreCase(l.getStatus()))
                .count();

        Map<String, Integer> deptCounts = employeesMap.values().stream()
                .collect(Collectors.groupingBy(EmployeeDto::getDepartment, Collectors.summingInt(e -> 1)));

        List<EmployeeDto> leadership = employeesMap.values().stream()
                .filter(e -> "Executive".equalsIgnoreCase(e.getDepartment()) || e.getJobTitle().startsWith("Chief"))
                .collect(Collectors.toList());

        List<LeaveRequestDto> recent = leavesMap.values().stream()
                .sorted(Comparator.comparing(LeaveRequestDto::getRequestedAt).reversed())
                .collect(Collectors.toList());

        double attendanceRate = total > 0 ? (active * 100.0 / total) : 100.0;

        return new WorkforceSummaryDto(
                total, active, onLeave, onDuty, pendingLeaves, attendanceRate,
                deptCounts, leadership, todaysShifts, recent
        );
    }

    public List<EmployeeDto> getAllEmployees(String department, String status) {
        return employeesMap.values().stream()
                .filter(e -> department == null || department.isBlank() || department.equalsIgnoreCase(e.getDepartment()))
                .filter(e -> status == null || status.isBlank() || status.equalsIgnoreCase(e.getStatus()))
                .sorted(Comparator.comparing(EmployeeDto::getEmployeeNumber))
                .collect(Collectors.toList());
    }

    public EmployeeDto getEmployeeById(String id) {
        return employeesMap.get(id);
    }

    public EmployeeDto createEmployee(CreateEmployeeRequest req) {
        String id = "emp-" + UUID.randomUUID().toString().substring(0, 8);
        String empNum = "MX-" + (1000 + employeesMap.size() + 1);

        String managerName = req.getManagerName();
        if (managerName == null && req.getManagerId() != null) {
            EmployeeDto mgr = employeesMap.get(req.getManagerId());
            if (mgr != null) managerName = mgr.getFullName();
        }

        EmployeeDto dto = new EmployeeDto(
                id,
                empNum,
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getPhone() != null ? req.getPhone() : "+1 (415) 555-0199",
                req.getDepartment(),
                req.getJobTitle(),
                req.getManagerId(),
                managerName,
                req.getEmploymentType() != null ? req.getEmploymentType() : "FULL_TIME",
                req.getWorkLocation() != null ? req.getWorkLocation() : "HEADQUARTERS",
                "ACTIVE",
                req.getHireDate() != null ? req.getHireDate() : LocalDate.now().toString(),
                req.getRemainingPtoDays() > 0 ? req.getRemainingPtoDays() : 20,
                Instant.now()
        );

        employeesMap.put(id, dto);
        log.info("Created new workforce employee: {} ({})", dto.getFullName(), dto.getId());
        return dto;
    }

    public List<ShiftAssignmentDto> getAllShifts(String startDate, String endDate, String employeeId) {
        return shiftsMap.values().stream()
                .filter(s -> employeeId == null || employeeId.isBlank() || employeeId.equalsIgnoreCase(s.getEmployeeId()))
                .filter(s -> startDate == null || startDate.isBlank() || s.getShiftDate().compareTo(startDate) >= 0)
                .filter(s -> endDate == null || endDate.isBlank() || s.getShiftDate().compareTo(endDate) <= 0)
                .sorted(Comparator.comparing(ShiftAssignmentDto::getShiftDate).thenComparing(ShiftAssignmentDto::getStartTime))
                .collect(Collectors.toList());
    }

    public ShiftAssignmentDto assignShift(CreateShiftRequest req) {
        String id = "shift-" + UUID.randomUUID().toString().substring(0, 8);
        EmployeeDto emp = employeesMap.get(req.getEmployeeId());

        ShiftAssignmentDto dto = new ShiftAssignmentDto(
                id,
                req.getEmployeeId(),
                emp != null ? emp.getFullName() : "Employee",
                emp != null ? emp.getDepartment() : "General",
                req.getShiftDate(),
                req.getShiftType(),
                req.getStartTime() != null ? req.getStartTime() : "08:00",
                req.getEndTime() != null ? req.getEndTime() : "16:30",
                req.getScheduledHours() > 0 ? req.getScheduledHours() : 8.0,
                req.getLocation() != null ? req.getLocation() : "HEADQUARTERS",
                "SCHEDULED",
                Instant.now()
        );

        shiftsMap.put(id, dto);
        log.info("Assigned shift: {} to {}", dto.getId(), dto.getEmployeeName());
        return dto;
    }

    public boolean deleteShift(String id) {
        return shiftsMap.remove(id) != null;
    }

    public List<LeaveRequestDto> getAllLeaveRequests(String status, String employeeId) {
        return leavesMap.values().stream()
                .filter(l -> status == null || status.isBlank() || status.equalsIgnoreCase(l.getStatus()))
                .filter(l -> employeeId == null || employeeId.isBlank() || employeeId.equalsIgnoreCase(l.getEmployeeId()))
                .sorted(Comparator.comparing(LeaveRequestDto::getRequestedAt).reversed())
                .collect(Collectors.toList());
    }

    public LeaveRequestDto submitLeaveRequest(CreateLeaveRequest req) {
        String id = "leave-" + UUID.randomUUID().toString().substring(0, 8);
        EmployeeDto emp = employeesMap.get(req.getEmployeeId());

        LeaveRequestDto dto = new LeaveRequestDto(
                id,
                req.getEmployeeId(),
                emp != null ? emp.getFullName() : "Staff Member",
                emp != null ? emp.getDepartment() : "General",
                req.getLeaveType(),
                req.getStartDate(),
                req.getEndDate(),
                req.getTotalDays() > 0 ? req.getTotalDays() : 1,
                req.getReason(),
                "PENDING",
                null,
                null,
                Instant.now(),
                null
        );

        leavesMap.put(id, dto);
        log.info("Submitted leave request: {} for {}", dto.getId(), dto.getEmployeeName());
        return dto;
    }

    public LeaveRequestDto updateLeaveStatus(String id, UpdateLeaveStatusRequest req) {
        LeaveRequestDto existing = leavesMap.get(id);
        if (existing == null) return null;

        existing.setStatus(req.getStatus().toUpperCase());
        existing.setReviewNotes(req.getReviewNotes());
        existing.setApprovedBy(req.getApprovedBy() != null ? req.getApprovedBy() : "Workforce Administrator");
        existing.setReviewedAt(Instant.now());

        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            EmployeeDto emp = employeesMap.get(existing.getEmployeeId());
            if (emp != null) {
                emp.setRemainingPtoDays(Math.max(0, emp.getRemainingPtoDays() - existing.getTotalDays()));
            }
        }

        log.info("Updated leave status: {} to {}", id, req.getStatus());
        return existing;
    }
}
