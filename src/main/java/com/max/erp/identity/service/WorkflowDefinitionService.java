package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.entity.*;
import com.max.erp.identity.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowDefinitionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public WorkflowDefinitionService(
            WorkflowRepository workflowRepository,
            WorkflowStepRepository workflowStepRepository,
            BusinessObjectRepository businessObjectRepository,
            RoleRepository roleRepository,
            UserRepository userRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.businessObjectRepository = businessObjectRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkflowResponse> getWorkflows(UUID tenantId, UUID businessObjectId) {
        List<Workflow> workflows;
        if (businessObjectId != null) {
            workflows = workflowRepository.findByTenantIdAndBusinessObjectIdOrderByNameAsc(tenantId, businessObjectId);
        } else {
            workflows = workflowRepository.findByTenantIdOrderByNameAsc(tenantId);
        }

        return workflows.stream()
                .map(this::toWorkflowResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(UUID tenantId, UUID workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        return toWorkflowResponse(workflow);
    }

    public WorkflowResponse createWorkflow(UUID tenantId, CreateWorkflowRequest request) {
        if (workflowRepository.existsByTenantIdAndWorkflowKey(tenantId, request.getWorkflowKey())) {
            throw new IllegalArgumentException("A workflow with key '" + request.getWorkflowKey() + "' already exists for this tenant.");
        }

        BusinessObject bo = businessObjectRepository.findById(request.getBusinessObjectId())
                .orElseThrow(() -> new IllegalArgumentException("Business object not found: " + request.getBusinessObjectId()));

        Workflow workflow = new Workflow();
        workflow.setTenantId(tenantId);
        workflow.setBusinessObjectId(bo.getId());
        workflow.setWorkflowKey(request.getWorkflowKey().trim().toLowerCase());
        workflow.setName(request.getName().trim());
        workflow.setDescription(request.getDescription());
        if (request.getTriggerType() != null) {
            workflow.setTriggerType(request.getTriggerType());
        }
        if (request.getStatus() != null) {
            workflow.setStatus(request.getStatus());
        }

        Workflow savedWorkflow = workflowRepository.save(workflow);

        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            int order = 1;
            for (CreateWorkflowStepRequest stepReq : request.getSteps()) {
                WorkflowStep step = new WorkflowStep();
                step.setWorkflow(savedWorkflow);
                step.setStepOrder(stepReq.getStepOrder() != null ? stepReq.getStepOrder() : order);
                step.setName(stepReq.getName().trim());
                if (stepReq.getStepType() != null) {
                    step.setStepType(stepReq.getStepType());
                }
                step.setApproverRoleId(stepReq.getApproverRoleId());
                step.setApproverUserId(stepReq.getApproverUserId());
                step.setConditionExpression(stepReq.getConditionExpression());
                if (stepReq.getStatusOnApproval() != null && !stepReq.getStatusOnApproval().isBlank()) {
                    step.setStatusOnApproval(stepReq.getStatusOnApproval());
                }
                if (stepReq.getStatusOnRejection() != null && !stepReq.getStatusOnRejection().isBlank()) {
                    step.setStatusOnRejection(stepReq.getStatusOnRejection());
                }
                step.setTimeoutHours(stepReq.getTimeoutHours());

                workflowStepRepository.save(step);
                savedWorkflow.getSteps().add(step);
                order++;
            }
        }

        return toWorkflowResponse(savedWorkflow);
    }

    public WorkflowResponse updateWorkflow(UUID tenantId, UUID workflowId, UpdateWorkflowRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        if (request.getName() != null && !request.getName().isBlank()) {
            workflow.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            workflow.setDescription(request.getDescription());
        }
        if (request.getTriggerType() != null) {
            workflow.setTriggerType(request.getTriggerType());
        }
        if (request.getStatus() != null) {
            workflow.setStatus(request.getStatus());
        }

        Workflow updated = workflowRepository.save(workflow);
        return toWorkflowResponse(updated);
    }

    public void deleteWorkflow(UUID tenantId, UUID workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        workflowRepository.delete(workflow);
    }

    public WorkflowStepResponse addStep(UUID tenantId, UUID workflowId, CreateWorkflowStepRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        List<WorkflowStep> existingSteps = workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId);
        int nextOrder = existingSteps.isEmpty() ? 1 : existingSteps.get(existingSteps.size() - 1).getStepOrder() + 1;
        int targetOrder = request.getStepOrder() != null ? request.getStepOrder() : nextOrder;

        WorkflowStep step = new WorkflowStep();
        step.setWorkflow(workflow);
        step.setStepOrder(targetOrder);
        step.setName(request.getName().trim());
        if (request.getStepType() != null) {
            step.setStepType(request.getStepType());
        }
        step.setApproverRoleId(request.getApproverRoleId());
        step.setApproverUserId(request.getApproverUserId());
        step.setConditionExpression(request.getConditionExpression());
        if (request.getStatusOnApproval() != null && !request.getStatusOnApproval().isBlank()) {
            step.setStatusOnApproval(request.getStatusOnApproval());
        }
        if (request.getStatusOnRejection() != null && !request.getStatusOnRejection().isBlank()) {
            step.setStatusOnRejection(request.getStatusOnRejection());
        }
        step.setTimeoutHours(request.getTimeoutHours());

        WorkflowStep saved = workflowStepRepository.save(step);
        return toStepResponse(saved);
    }

    public WorkflowStepResponse updateStep(UUID tenantId, UUID workflowId, UUID stepId, CreateWorkflowStepRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        WorkflowStep step = workflowStepRepository.findById(stepId)
                .filter(s -> s.getWorkflow().getId().equals(workflow.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Workflow step not found for ID: " + stepId));

        if (request.getName() != null && !request.getName().isBlank()) {
            step.setName(request.getName().trim());
        }
        if (request.getStepOrder() != null) {
            step.setStepOrder(request.getStepOrder());
        }
        if (request.getStepType() != null) {
            step.setStepType(request.getStepType());
        }
        step.setApproverRoleId(request.getApproverRoleId());
        step.setApproverUserId(request.getApproverUserId());
        step.setConditionExpression(request.getConditionExpression());
        if (request.getStatusOnApproval() != null && !request.getStatusOnApproval().isBlank()) {
            step.setStatusOnApproval(request.getStatusOnApproval());
        }
        if (request.getStatusOnRejection() != null && !request.getStatusOnRejection().isBlank()) {
            step.setStatusOnRejection(request.getStatusOnRejection());
        }
        step.setTimeoutHours(request.getTimeoutHours());

        WorkflowStep updated = workflowStepRepository.save(step);
        return toStepResponse(updated);
    }

    public void deleteStep(UUID tenantId, UUID workflowId, UUID stepId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        WorkflowStep step = workflowStepRepository.findById(stepId)
                .filter(s -> s.getWorkflow().getId().equals(workflow.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Workflow step not found for ID: " + stepId));

        workflowStepRepository.delete(step);
    }

    public WorkflowResponse toWorkflowResponse(Workflow workflow) {
        WorkflowResponse resp = new WorkflowResponse();
        resp.setId(workflow.getId());
        resp.setTenantId(workflow.getTenantId());
        resp.setBusinessObjectId(workflow.getBusinessObjectId());
        resp.setWorkflowKey(workflow.getWorkflowKey());
        resp.setName(workflow.getName());
        resp.setDescription(workflow.getDescription());
        resp.setTriggerType(workflow.getTriggerType());
        resp.setStatus(workflow.getStatus());
        resp.setCreatedAt(workflow.getCreatedAt());
        resp.setUpdatedAt(workflow.getUpdatedAt());

        businessObjectRepository.findById(workflow.getBusinessObjectId()).ifPresent(bo -> {
            resp.setBusinessObjectName(bo.getName());
            resp.setBusinessObjectKey(bo.getObjectKey());
        });

        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(workflow.getId());
        resp.setSteps(steps.stream().map(this::toStepResponse).collect(Collectors.toList()));

        return resp;
    }

    public WorkflowStepResponse toStepResponse(WorkflowStep step) {
        WorkflowStepResponse resp = new WorkflowStepResponse();
        resp.setId(step.getId());
        resp.setWorkflowId(step.getWorkflow() != null ? step.getWorkflow().getId() : null);
        resp.setStepOrder(step.getStepOrder());
        resp.setName(step.getName());
        resp.setStepType(step.getStepType());
        resp.setApproverRoleId(step.getApproverRoleId());
        resp.setApproverUserId(step.getApproverUserId());
        resp.setConditionExpression(step.getConditionExpression());
        resp.setStatusOnApproval(step.getStatusOnApproval());
        resp.setStatusOnRejection(step.getStatusOnRejection());
        resp.setTimeoutHours(step.getTimeoutHours());
        resp.setCreatedAt(step.getCreatedAt());
        resp.setUpdatedAt(step.getUpdatedAt());

        if (step.getApproverRoleId() != null) {
            roleRepository.findById(step.getApproverRoleId()).ifPresent(role -> resp.setApproverRoleName(role.getName()));
        }

        if (step.getApproverUserId() != null) {
            userRepository.findById(step.getApproverUserId()).ifPresent(user -> resp.setApproverUsername(user.getUsername()));
        }

        return resp;
    }
}
