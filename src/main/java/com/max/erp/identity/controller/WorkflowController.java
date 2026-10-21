package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.WorkflowDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/workflows")
public class WorkflowController {

    private final WorkflowDefinitionService workflowDefinitionService;

    public WorkflowController(WorkflowDefinitionService workflowDefinitionService) {
        this.workflowDefinitionService = workflowDefinitionService;
    }

    @GetMapping
    public ResponseEntity<List<WorkflowResponse>> getWorkflows(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) UUID businessObjectId
    ) {
        return ResponseEntity.ok(workflowDefinitionService.getWorkflows(tenantId, businessObjectId));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<WorkflowResponse> getWorkflow(
            @PathVariable UUID tenantId,
            @PathVariable UUID workflowId
    ) {
        return ResponseEntity.ok(workflowDefinitionService.getWorkflow(tenantId, workflowId));
    }

    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateWorkflowRequest request
    ) {
        WorkflowResponse response = workflowDefinitionService.createWorkflow(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{workflowId}")
    public ResponseEntity<WorkflowResponse> updateWorkflow(
            @PathVariable UUID tenantId,
            @PathVariable UUID workflowId,
            @RequestBody UpdateWorkflowRequest request
    ) {
        return ResponseEntity.ok(workflowDefinitionService.updateWorkflow(tenantId, workflowId, request));
    }

    @DeleteMapping("/{workflowId}")
    public ResponseEntity<Void> deleteWorkflow(
            @PathVariable UUID tenantId,
            @PathVariable UUID workflowId
    ) {
        workflowDefinitionService.deleteWorkflow(tenantId, workflowId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workflowId}/steps")
    public ResponseEntity<WorkflowStepResponse> addStep(
            @PathVariable UUID tenantId,
            @PathVariable UUID workflowId,
            @Valid @RequestBody CreateWorkflowStepRequest request
    ) {
        WorkflowStepResponse response = workflowDefinitionService.addStep(tenantId, workflowId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{workflowId}/steps/{stepId}")
    public ResponseEntity<WorkflowStepResponse> updateStep(
            @PathVariable UUID tenantId,
            @PathVariable UUID workflowId,
            @PathVariable UUID stepId,
            @Valid @RequestBody CreateWorkflowStepRequest request
    ) {
        return ResponseEntity.ok(workflowDefinitionService.updateStep(tenantId, workflowId, stepId, request));
    }

    @DeleteMapping("/{workflowId}/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(
            @PathVariable UUID tenantId,
            @PathVariable UUID workflowId,
            @PathVariable UUID stepId
    ) {
        workflowDefinitionService.deleteStep(tenantId, workflowId, stepId);
        return ResponseEntity.noContent().build();
    }
}
