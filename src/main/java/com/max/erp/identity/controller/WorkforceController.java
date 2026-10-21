package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.WorkforceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workforce")
public class WorkforceController {

    private final WorkforceService workforceService;

    public WorkforceController(WorkforceService workforceService) {
        this.workforceService = workforceService;
    }

    @GetMapping("/summary")
    public ResponseEntity<WorkforceSummaryDto> getSummary() {
        return ResponseEntity.ok(workforceService.getWorkforceSummary());
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDto>> getEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(workforceService.getAllEmployees(department, status));
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable String id) {
        EmployeeDto emp = workforceService.getEmployeeById(id);
        if (emp == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(emp);
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDto created = workforceService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/shifts")
    public ResponseEntity<List<ShiftAssignmentDto>> getShifts(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String employeeId) {
        return ResponseEntity.ok(workforceService.getAllShifts(startDate, endDate, employeeId));
    }

    @PostMapping("/shifts")
    public ResponseEntity<ShiftAssignmentDto> assignShift(@Valid @RequestBody CreateShiftRequest request) {
        ShiftAssignmentDto created = workforceService.assignShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/shifts/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable String id) {
        boolean removed = workforceService.deleteShift(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequestDto>> getLeaves(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeId) {
        return ResponseEntity.ok(workforceService.getAllLeaveRequests(status, employeeId));
    }

    @PostMapping("/leaves")
    public ResponseEntity<LeaveRequestDto> submitLeave(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveRequestDto created = workforceService.submitLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/leaves/{id}/status")
    public ResponseEntity<LeaveRequestDto> updateLeaveStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateLeaveStatusRequest request) {
        LeaveRequestDto updated = workforceService.updateLeaveStatus(id, request);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }
}
