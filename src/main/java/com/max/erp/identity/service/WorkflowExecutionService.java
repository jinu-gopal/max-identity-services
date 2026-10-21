package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.entity.*;
import com.max.erp.identity.enums.WorkflowActionType;
import com.max.erp.identity.enums.WorkflowInstanceStatus;
import com.max.erp.identity.enums.WorkflowStatus;
import com.max.erp.identity.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowExecutionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutionService.class);
    private static final Pattern SIMPLE_CONDITION_PATTERN =
            Pattern.compile("^\\s*([a-zA-Z0-9_]+)\\s*(>=|<=|==|!=|>|<)\\s*['\"]?([^'\"]+)['\"]?\\s*$");

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowActionRepository workflowActionRepository;
    private final BusinessObjectRecordRepository recordRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public WorkflowExecutionService(
            WorkflowRepository workflowRepository,
            WorkflowStepRepository workflowStepRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            WorkflowActionRepository workflowActionRepository,
            BusinessObjectRecordRepository recordRepository,
            BusinessObjectRepository businessObjectRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowActionRepository = workflowActionRepository;
        this.recordRepository = recordRepository;
        this.businessObjectRepository = businessObjectRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public WorkflowInstanceResponse submitRecordToWorkflow(
            UUID tenantId,
            UUID workflowId,
            SubmitWorkflowInstanceRequest request,
            UUID initiatedByUserId
    ) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(w -> w.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found for ID: " + workflowId));

        if (workflow.getStatus() != WorkflowStatus.ACTIVE) {
            throw new IllegalArgumentException("Workflow '" + workflow.getName() + "' is not active.");
        }

        BusinessObjectRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Record not found for ID: " + request.getRecordId()));

        if (!record.getBusinessObjectId().equals(workflow.getBusinessObjectId())) {
            throw new IllegalArgumentException("Record business object does not match workflow business object.");
        }

        // Snapshot current record data combined with optional context data
        Map<String, Object> combinedContext = new LinkedHashMap<>();
        if (record.getRecordData() != null) {
            combinedContext.putAll(record.getRecordData());
        }
        if (request.getContextData() != null) {
            combinedContext.putAll(request.getContextData());
        }

        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(workflow.getId());
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow '" + workflow.getName() + "' has no steps configured.");
        }

        // Find first step that matches conditions
        WorkflowStep firstStep = null;
        for (WorkflowStep step : steps) {
            if (evaluateCondition(step.getConditionExpression(), combinedContext)) {
                firstStep = step;
                break;
            }
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.setTenantId(tenantId);
        instance.setWorkflowId(workflow.getId());
        instance.setBusinessObjectId(workflow.getBusinessObjectId());
        instance.setRecordId(record.getId());
        instance.setInitiatedBy(initiatedByUserId);
        instance.setInitiatedAt(Instant.now());
        instance.setContextData(combinedContext);

        if (firstStep != null) {
            instance.setCurrentStepId(firstStep.getId());
            instance.setCurrentStepOrder(firstStep.getStepOrder());
            instance.setStatus(WorkflowInstanceStatus.PENDING);
        } else {
            // All steps skipped because conditions didn't apply -> automatically approved
            instance.setCurrentStepId(null);
            instance.setCurrentStepOrder(steps.size());
            instance.setStatus(WorkflowInstanceStatus.APPROVED);
            instance.setCompletedAt(Instant.now());
        }

        WorkflowInstance savedInstance = workflowInstanceRepository.save(instance);

        // Update record workflow_status
        if (record.getRecordData() != null) {
            record.getRecordData().put("workflow_status",
                    savedInstance.getStatus() == WorkflowInstanceStatus.PENDING ? "PENDING_APPROVAL" : "APPROVED");
            record.setUpdatedAt(java.time.OffsetDateTime.now());
            recordRepository.save(record);
        }

        return toInstanceResponse(savedInstance);
    }

    public WorkflowInstanceResponse executeDecision(
            UUID tenantId,
            UUID instanceId,
            WorkflowDecisionRequest request,
            UUID actorUserId,
            boolean isAdmin
    ) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
                .filter(i -> i.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow instance not found for ID: " + instanceId));

        if (instance.getStatus() != WorkflowInstanceStatus.PENDING) {
            throw new IllegalArgumentException("Workflow instance is not pending (current status: " + instance.getStatus() + ")");
        }

        if (instance.getCurrentStepId() == null) {
            throw new IllegalArgumentException("No active step associated with workflow instance: " + instanceId);
        }

        WorkflowStep currentStep = workflowStepRepository.findById(instance.getCurrentStepId())
                .orElseThrow(() -> new IllegalArgumentException("Current step not found: " + instance.getCurrentStepId()));

        // Authorization check
        if (!isAdmin) {
            boolean authorized = false;
            if (currentStep.getApproverUserId() != null && currentStep.getApproverUserId().equals(actorUserId)) {
                authorized = true;
            } else if (currentStep.getApproverRoleId() != null) {
                List<UserRole> userRoles = userRoleRepository.findAllByUserIdAndTenantId(actorUserId, tenantId);
                for (UserRole ur : userRoles) {
                    if (ur.getRole() != null && ur.getRole().getId().equals(currentStep.getApproverRoleId())) {
                        authorized = true;
                        break;
                    }
                }
            }

            if (!authorized) {
                throw new IllegalArgumentException("You are not authorized to approve/reject this workflow step.");
            }
        }

        // Record Workflow Action audit
        WorkflowAction action = new WorkflowAction();
        action.setInstanceId(instance.getId());
        action.setStepId(currentStep.getId());
        action.setActorUserId(actorUserId);
        action.setAction(request.getAction());
        action.setComments(request.getComments().trim());
        action.setActionTimestamp(Instant.now());
        workflowActionRepository.save(action);

        BusinessObjectRecord record = recordRepository.findById(instance.getRecordId()).orElse(null);

        if (request.getAction() == WorkflowActionType.REJECTED) {
            instance.setStatus(WorkflowInstanceStatus.REJECTED);
            instance.setCompletedAt(Instant.now());

            if (record != null && record.getRecordData() != null) {
                record.getRecordData().put("workflow_status", currentStep.getStatusOnRejection());
                record.getRecordData().put("status", currentStep.getStatusOnRejection());
                record.setUpdatedAt(java.time.OffsetDateTime.now());
                recordRepository.save(record);
            }
        } else if (request.getAction() == WorkflowActionType.APPROVED) {
            // Find next step in workflow
            List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(instance.getWorkflowId());
            WorkflowStep nextStep = null;
            Map<String, Object> ctx = instance.getContextData() != null ? instance.getContextData() : Map.of();

            for (WorkflowStep s : steps) {
                if (s.getStepOrder() > currentStep.getStepOrder()) {
                    if (evaluateCondition(s.getConditionExpression(), ctx)) {
                        nextStep = s;
                        break;
                    }
                }
            }

            if (nextStep != null) {
                // Advance to next step
                instance.setCurrentStepId(nextStep.getId());
                instance.setCurrentStepOrder(nextStep.getStepOrder());
            } else {
                // All steps completed!
                instance.setStatus(WorkflowInstanceStatus.APPROVED);
                instance.setCurrentStepId(null);
                instance.setCompletedAt(Instant.now());

                if (record != null && record.getRecordData() != null) {
                    record.getRecordData().put("workflow_status", currentStep.getStatusOnApproval());
                    record.getRecordData().put("status", currentStep.getStatusOnApproval());
                    record.setUpdatedAt(java.time.OffsetDateTime.now());
                    recordRepository.save(record);
                }
            }
        } else if (request.getAction() == WorkflowActionType.DELEGATED) {
            if (request.getDelegatedToUserId() != null) {
                currentStep.setApproverUserId(request.getDelegatedToUserId());
                workflowStepRepository.save(currentStep);
            }
        }

        WorkflowInstance updated = workflowInstanceRepository.save(instance);
        return toInstanceResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<PendingApprovalResponse> getPendingApprovals(UUID tenantId, UUID userId, boolean isAdmin) {
        List<WorkflowInstance> pendingInstances;

        if (isAdmin) {
            pendingInstances = workflowInstanceRepository.findByTenantIdAndStatusOrderByInitiatedAtDesc(
                    tenantId, WorkflowInstanceStatus.PENDING
            );
        } else {
            List<UserRole> userRoles = userRoleRepository.findAllByUserIdAndTenantId(userId, tenantId);
            Set<UUID> roleIds = userRoles.stream()
                    .filter(ur -> ur.getRole() != null)
                    .map(ur -> ur.getRole().getId())
                    .collect(Collectors.toSet());

            if (roleIds.isEmpty()) {
                pendingInstances = workflowInstanceRepository.findPendingForUserOnly(tenantId, userId);
            } else {
                pendingInstances = workflowInstanceRepository.findPendingForUserOrRoles(tenantId, userId, roleIds);
            }
        }

        return pendingInstances.stream()
                .map(this::toPendingApprovalResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowInstanceResponse getInstanceDetail(UUID tenantId, UUID instanceId) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
                .filter(i -> i.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Workflow instance not found for ID: " + instanceId));

        return toInstanceResponse(instance);
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstanceResponse> getInstances(
            UUID tenantId,
            UUID businessObjectId,
            UUID recordId,
            WorkflowInstanceStatus status
    ) {
        List<WorkflowInstance> instances;
        if (recordId != null) {
            instances = workflowInstanceRepository.findByTenantIdAndRecordIdOrderByInitiatedAtDesc(tenantId, recordId);
        } else if (status != null) {
            instances = workflowInstanceRepository.findByTenantIdAndStatusOrderByInitiatedAtDesc(tenantId, status);
        } else {
            instances = workflowInstanceRepository.findByTenantIdOrderByInitiatedAtDesc(tenantId);
        }

        if (businessObjectId != null) {
            instances = instances.stream()
                    .filter(i -> i.getBusinessObjectId().equals(businessObjectId))
                    .collect(Collectors.toList());
        }

        return instances.stream()
                .map(this::toInstanceResponse)
                .collect(Collectors.toList());
    }

    public boolean evaluateCondition(String expression, Map<String, Object> data) {
        if (expression == null || expression.isBlank()) {
            return true;
        }

        String expr = expression.trim();

        // 1. Try Simple Regex pattern: field operator value
        Matcher matcher = SIMPLE_CONDITION_PATTERN.matcher(expr);
        if (matcher.matches()) {
            String field = matcher.group(1);
            String op = matcher.group(2);
            String expected = matcher.group(3);

            Object actual = data.get(field);
            if (actual == null) {
                return false;
            }

            try {
                // If numeric comparison
                if (actual instanceof Number || isNumeric(expected)) {
                    double actualNum = Double.parseDouble(actual.toString());
                    double expectedNum = Double.parseDouble(expected);
                    return switch (op) {
                        case ">" -> actualNum > expectedNum;
                        case ">=" -> actualNum >= expectedNum;
                        case "<" -> actualNum < expectedNum;
                        case "<=" -> actualNum <= expectedNum;
                        case "==" -> Math.abs(actualNum - expectedNum) < 0.00001;
                        case "!=" -> Math.abs(actualNum - expectedNum) >= 0.00001;
                        default -> false;
                    };
                } else {
                    String actualStr = actual.toString();
                    return switch (op) {
                        case "==" -> actualStr.equalsIgnoreCase(expected);
                        case "!=" -> !actualStr.equalsIgnoreCase(expected);
                        default -> false;
                    };
                }
            } catch (Exception e) {
                log.warn("Error evaluating simple condition '{}' on field '{}': {}", expr, field, e.getMessage());
            }
        }

        // 2. Fallback to Spring SpEL
        try {
            StandardEvaluationContext context = new StandardEvaluationContext(data);
            context.setVariables(data);
            Boolean result = spelParser.parseExpression(expr).getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Error evaluating SpEL expression '{}': {}", expr, e.getMessage());
            return true; // Default to true if expression is invalid
        }
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public WorkflowInstanceResponse toInstanceResponse(WorkflowInstance instance) {
        WorkflowInstanceResponse resp = new WorkflowInstanceResponse();
        resp.setId(instance.getId());
        resp.setTenantId(instance.getTenantId());
        resp.setWorkflowId(instance.getWorkflowId());
        resp.setBusinessObjectId(instance.getBusinessObjectId());
        resp.setRecordId(instance.getRecordId());
        resp.setCurrentStepId(instance.getCurrentStepId());
        resp.setCurrentStepOrder(instance.getCurrentStepOrder());
        resp.setStatus(instance.getStatus());
        resp.setInitiatedBy(instance.getInitiatedBy());
        resp.setInitiatedAt(instance.getInitiatedAt());
        resp.setCompletedAt(instance.getCompletedAt());
        resp.setContextData(instance.getContextData());

        workflowRepository.findById(instance.getWorkflowId()).ifPresent(w -> {
            resp.setWorkflowName(w.getName());
            resp.setWorkflowKey(w.getWorkflowKey());
        });

        businessObjectRepository.findById(instance.getBusinessObjectId()).ifPresent(bo -> {
            resp.setBusinessObjectName(bo.getName());
            resp.setBusinessObjectKey(bo.getObjectKey());
        });

        userRepository.findById(instance.getInitiatedBy()).ifPresent(u -> {
            resp.setInitiatorUsername(u.getUsername());
        });

        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(instance.getWorkflowId());
        resp.setTotalSteps(steps.size());

        if (instance.getCurrentStepId() != null) {
            steps.stream().filter(s -> s.getId().equals(instance.getCurrentStepId())).findFirst().ifPresent(step -> {
                resp.setCurrentStepName(step.getName());
                resp.setApproverRoleId(step.getApproverRoleId());
                if (step.getApproverRoleId() != null) {
                    roleRepository.findById(step.getApproverRoleId()).ifPresent(r -> resp.setApproverRoleName(r.getName()));
                }
            });
        }

        // Attach actions audit timeline
        List<WorkflowAction> actions = workflowActionRepository.findByInstanceIdOrderByActionTimestampAsc(instance.getId());
        List<WorkflowActionResponse> actionResponses = actions.stream().map(a -> {
            WorkflowActionResponse ar = new WorkflowActionResponse();
            ar.setId(a.getId());
            ar.setInstanceId(a.getInstanceId());
            ar.setStepId(a.getStepId());
            ar.setAction(a.getAction());
            ar.setComments(a.getComments());
            ar.setActionTimestamp(a.getActionTimestamp());
            ar.setActorUserId(a.getActorUserId());

            userRepository.findById(a.getActorUserId()).ifPresent(u -> ar.setActorUsername(u.getUsername()));
            steps.stream().filter(s -> s.getId().equals(a.getStepId())).findFirst().ifPresent(s -> {
                ar.setStepName(s.getName());
                ar.setStepOrder(s.getStepOrder());
            });

            return ar;
        }).collect(Collectors.toList());
        resp.setActions(actionResponses);

        // Attach steps metadata for progress visualizer
        resp.setSteps(steps.stream().map(s -> {
            WorkflowStepResponse sr = new WorkflowStepResponse();
            sr.setId(s.getId());
            sr.setWorkflowId(s.getWorkflow() != null ? s.getWorkflow().getId() : null);
            sr.setStepOrder(s.getStepOrder());
            sr.setName(s.getName());
            sr.setStepType(s.getStepType());
            sr.setApproverRoleId(s.getApproverRoleId());
            sr.setApproverUserId(s.getApproverUserId());
            sr.setConditionExpression(s.getConditionExpression());
            sr.setStatusOnApproval(s.getStatusOnApproval());
            sr.setStatusOnRejection(s.getStatusOnRejection());
            if (s.getApproverRoleId() != null) {
                roleRepository.findById(s.getApproverRoleId()).ifPresent(r -> sr.setApproverRoleName(r.getName()));
            }
            return sr;
        }).collect(Collectors.toList()));

        return resp;
    }

    private PendingApprovalResponse toPendingApprovalResponse(WorkflowInstance instance) {
        PendingApprovalResponse resp = new PendingApprovalResponse();
        resp.setInstanceId(instance.getId());
        resp.setTenantId(instance.getTenantId());
        resp.setWorkflowId(instance.getWorkflowId());
        resp.setBusinessObjectId(instance.getBusinessObjectId());
        resp.setRecordId(instance.getRecordId());
        resp.setStepId(instance.getCurrentStepId());
        resp.setStepOrder(instance.getCurrentStepOrder());
        resp.setInitiatedBy(instance.getInitiatedBy());
        resp.setInitiatedAt(instance.getInitiatedAt());
        resp.setContextData(instance.getContextData());

        workflowRepository.findById(instance.getWorkflowId()).ifPresent(w -> resp.setWorkflowName(w.getName()));
        businessObjectRepository.findById(instance.getBusinessObjectId()).ifPresent(bo -> {
            resp.setBusinessObjectName(bo.getName());
            resp.setBusinessObjectKey(bo.getObjectKey());
        });
        userRepository.findById(instance.getInitiatedBy()).ifPresent(u -> resp.setInitiatorUsername(u.getUsername()));

        if (instance.getCurrentStepId() != null) {
            workflowStepRepository.findById(instance.getCurrentStepId()).ifPresent(s -> {
                resp.setStepName(s.getName());
                resp.setApproverRoleId(s.getApproverRoleId());
                resp.setApproverUserId(s.getApproverUserId());
                if (s.getApproverRoleId() != null) {
                    roleRepository.findById(s.getApproverRoleId()).ifPresent(r -> resp.setApproverRoleName(r.getName()));
                }
            });
        }

        return resp;
    }
}
