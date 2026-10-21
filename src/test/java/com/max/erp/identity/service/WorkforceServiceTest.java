package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkforceServiceTest {

    private WorkforceService workforceService;

    @BeforeEach
    void setUp() {
        workforceService = new WorkforceService();
    }

    @Test
    void testWorkforceSummary() {
        WorkforceSummaryDto summary = workforceService.getWorkforceSummary();
        assertNotNull(summary);
        assertTrue(summary.getTotalEmployees() >= 10);
        assertTrue(summary.getActiveEmployees() >= 8);
        assertTrue(summary.getOnDutyToday() >= 2);
        assertTrue(summary.getPendingLeaveRequests() >= 2);
        assertTrue(summary.getOverallAttendanceRate() > 80.0);
        assertNotNull(summary.getDepartmentHeadcount());
        assertTrue(summary.getDepartmentHeadcount().containsKey("Engineering"));
        assertFalse(summary.getLeadershipTeam().isEmpty());
    }

    @Test
    void testGetAllEmployeesAndFilter() {
        List<EmployeeDto> all = workforceService.getAllEmployees(null, null);
        assertNotNull(all);
        assertTrue(all.size() >= 10);

        List<EmployeeDto> eng = workforceService.getAllEmployees("Engineering", null);
        assertNotNull(eng);
        assertFalse(eng.isEmpty());
        assertTrue(eng.stream().allMatch(e -> "Engineering".equalsIgnoreCase(e.getDepartment())));

        EmployeeDto first = all.get(0);
        EmployeeDto found = workforceService.getEmployeeById(first.getId());
        assertNotNull(found);
        assertEquals(first.getFullName(), found.getFullName());
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setFirstName("Clara");
        req.setLastName("Oswald");
        req.setEmail("clara.oswald@max-erp.global");
        req.setDepartment("Operations");
        req.setJobTitle("Field Logistics Specialist");
        req.setEmploymentType("FULL_TIME");
        req.setWorkLocation("REGIONAL_HUB");
        req.setRemainingPtoDays(22);

        EmployeeDto created = workforceService.createEmployee(req);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Clara Oswald", created.getFullName());
        assertEquals("Operations", created.getDepartment());
        assertEquals(22, created.getRemainingPtoDays());

        EmployeeDto verified = workforceService.getEmployeeById(created.getId());
        assertNotNull(verified);
        assertEquals("Clara Oswald", verified.getFullName());
    }

    @Test
    void testAssignAndCancelShift() {
        CreateShiftRequest req = new CreateShiftRequest();
        req.setEmployeeId("emp-005");
        req.setShiftDate("2026-10-20");
        req.setShiftType("MORNING");
        req.setStartTime("08:00");
        req.setEndTime("16:30");
        req.setScheduledHours(8.0);
        req.setLocation("HEADQUARTERS");

        ShiftAssignmentDto shift = workforceService.assignShift(req);
        assertNotNull(shift);
        assertNotNull(shift.getId());
        assertEquals("emp-005", shift.getEmployeeId());
        assertEquals("Priya Sharma", shift.getEmployeeName());
        assertEquals("SCHEDULED", shift.getStatus());

        List<ShiftAssignmentDto> shifts = workforceService.getAllShifts("2026-10-20", "2026-10-20", "emp-005");
        assertFalse(shifts.isEmpty());

        boolean removed = workforceService.deleteShift(shift.getId());
        assertTrue(removed);
    }

    @Test
    void testLeaveWorkflowApproval() {
        CreateLeaveRequest req = new CreateLeaveRequest();
        req.setEmployeeId("emp-002"); // Marcus Chen (CTO, 18 days PTO)
        req.setLeaveType("VACATION");
        req.setStartDate("2026-11-01");
        req.setEndDate("2026-11-03");
        req.setTotalDays(3);
        req.setReason("Tech conference attendance & personal rest");

        LeaveRequestDto leave = workforceService.submitLeaveRequest(req);
        assertNotNull(leave);
        assertEquals("PENDING", leave.getStatus());
        assertEquals(3, leave.getTotalDays());

        EmployeeDto empBefore = workforceService.getEmployeeById("emp-002");
        int initialPto = empBefore.getRemainingPtoDays();

        UpdateLeaveStatusRequest updateReq = new UpdateLeaveStatusRequest();
        updateReq.setStatus("APPROVED");
        updateReq.setApprovedBy("Elena Vance");
        updateReq.setReviewNotes("Approved. Enjoy the conference!");

        LeaveRequestDto approved = workforceService.updateLeaveStatus(leave.getId(), updateReq);
        assertNotNull(approved);
        assertEquals("APPROVED", approved.getStatus());
        assertEquals("Elena Vance", approved.getApprovedBy());

        EmployeeDto empAfter = workforceService.getEmployeeById("emp-002");
        assertEquals(initialPto - 3, empAfter.getRemainingPtoDays());
    }
}
