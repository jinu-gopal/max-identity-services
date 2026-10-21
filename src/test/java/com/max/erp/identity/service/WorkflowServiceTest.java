package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.entity.*;
import com.max.erp.identity.enums.WorkflowActionType;
import com.max.erp.identity.enums.WorkflowInstanceStatus;
import com.max.erp.identity.enums.WorkflowStatus;
import com.max.erp.identity.enums.WorkflowStepType;
import com.max.erp.identity.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Mock
    private WorkflowActionRepository workflowActionRepository;

    @Mock
    private BusinessObjectRecordRepository recordRepository;

    @Mock
    private BusinessObjectRepository businessObjectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    private WorkflowDefinitionService definitionService;
    private WorkflowExecutionService executionService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID businessObjectId = UUID.randomUUID();
    private final UUID workflowId = UUID.randomUUID();
    private final UUID recordId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID roleId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        definitionService = new WorkflowDefinitionService(
                workflowRepository,
                workflowStepRepository,
                businessObjectRepository,
                roleRepository,
                userRepository
        );

        executionService = new WorkflowExecutionService(
                workflowRepository,
                workflowStepRepository,
                workflowInstanceRepository,
                workflowActionRepository,
                recordRepository,
                businessObjectRepository,
                userRepository,
                roleRepository,
                userRoleRepository
        );
    }

    @Test
    void testCreateWorkflowSuccess() {
        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);
        bo.setName("Sales Order");
        bo.setObjectKey("sales_order");

        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(bo));
        when(workflowRepository.existsByTenantIdAndWorkflowKey(tenantId, "so_approval")).thenReturn(false);
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> {
            Workflow w = invocation.getArgument(0);
            w.setId(workflowId);
            return w;
        });

        CreateWorkflowRequest request = new CreateWorkflowRequest();
        request.setBusinessObjectId(businessObjectId);
        request.setWorkflowKey("so_approval");
        request.setName("Sales Order Approval");
        request.setDescription("Multi-tier approval for orders");

        WorkflowResponse response = definitionService.createWorkflow(tenantId, request);

        assertNotNull(response);
        assertEquals(workflowId, response.getId());
        assertEquals("so_approval", response.getWorkflowKey());
        assertEquals("Sales Order Approval", response.getName());
    }

    @Test
    void testConditionEvaluation() {
        Map<String, Object> data = new HashMap<>();
        data.put("total_amount", 75000);
        data.put("industry", "MANUFACTURING");
        data.put("customer_type", "ENTERPRISE");

        assertTrue(executionService.evaluateCondition("total_amount > 50000", data));
        assertFalse(executionService.evaluateCondition("total_amount > 100000", data));
        assertTrue(executionService.evaluateCondition("total_amount >= 75000", data));
        assertTrue(executionService.evaluateCondition("industry == 'MANUFACTURING'", data));
        assertFalse(executionService.evaluateCondition("industry == 'HEALTHCARE'", data));
        assertTrue(executionService.evaluateCondition(null, data));
        assertTrue(executionService.evaluateCondition("", data));
    }

    @Test
    void testSubmitRecordToWorkflow() {
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setTenantId(tenantId);
        workflow.setBusinessObjectId(businessObjectId);
        workflow.setName("Order Approval");
        workflow.setStatus(WorkflowStatus.ACTIVE);

        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setBusinessObjectId(businessObjectId);
        Map<String, Object> recordData = new HashMap<>();
        recordData.put("total_amount", 120000);
        record.setRecordData(recordData);

        WorkflowStep step1 = new WorkflowStep();
        step1.setId(UUID.randomUUID());
        step1.setStepOrder(1);
        step1.setName("Finance Review");
        step1.setApproverRoleId(roleId);
        step1.setConditionExpression("total_amount > 50000");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId)).thenReturn(List.of(step1));
        when(workflowInstanceRepository.save(any(WorkflowInstance.class))).thenAnswer(invocation -> {
            WorkflowInstance wi = invocation.getArgument(0);
            wi.setId(UUID.randomUUID());
            return wi;
        });

        SubmitWorkflowInstanceRequest req = new SubmitWorkflowInstanceRequest();
        req.setRecordId(recordId);

        WorkflowInstanceResponse res = executionService.submitRecordToWorkflow(tenantId, workflowId, req, userId);

        assertNotNull(res);
        assertEquals(WorkflowInstanceStatus.PENDING, res.getStatus());
        assertEquals(1, res.getCurrentStepOrder());
        assertEquals("PENDING_APPROVAL", record.getRecordData().get("workflow_status"));
    }

    @Test
    void testExecuteDecisionApproveAdvancesStep() {
        UUID instanceId = UUID.randomUUID();
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();

        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(instanceId);
        instance.setTenantId(tenantId);
        instance.setWorkflowId(workflowId);
        instance.setBusinessObjectId(businessObjectId);
        instance.setRecordId(recordId);
        instance.setCurrentStepId(step1Id);
        instance.setCurrentStepOrder(1);
        instance.setStatus(WorkflowInstanceStatus.PENDING);
        instance.setContextData(Map.of("total_amount", 100000));

        WorkflowStep step1 = new WorkflowStep();
        step1.setId(step1Id);
        step1.setStepOrder(1);
        step1.setName("Lead Approval");
        step1.setApproverUserId(userId);

        WorkflowStep step2 = new WorkflowStep();
        step2.setId(step2Id);
        step2.setStepOrder(2);
        step2.setName("VP Executive Signoff");
        step2.setApproverRoleId(roleId);

        when(workflowInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(workflowStepRepository.findById(step1Id)).thenReturn(Optional.of(step1));
        when(workflowStepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId)).thenReturn(List.of(step1, step2));
        when(workflowInstanceRepository.save(any(WorkflowInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowDecisionRequest req = new WorkflowDecisionRequest();
        req.setAction(WorkflowActionType.APPROVED);
        req.setComments("Lead approval granted, verified budget.");

        WorkflowInstanceResponse res = executionService.executeDecision(tenantId, instanceId, req, userId, false);

        assertNotNull(res);
        assertEquals(step2Id, res.getCurrentStepId());
        assertEquals(2, res.getCurrentStepOrder());
        assertEquals(WorkflowInstanceStatus.PENDING, res.getStatus());
        verify(workflowActionRepository, times(1)).save(any(WorkflowAction.class));
    }

    @Test
    void testExecuteDecisionRejectTerminatesWorkflow() {
        UUID instanceId = UUID.randomUUID();
        UUID step1Id = UUID.randomUUID();

        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(instanceId);
        instance.setTenantId(tenantId);
        instance.setWorkflowId(workflowId);
        instance.setBusinessObjectId(businessObjectId);
        instance.setRecordId(recordId);
        instance.setCurrentStepId(step1Id);
        instance.setCurrentStepOrder(1);
        instance.setStatus(WorkflowInstanceStatus.PENDING);

        WorkflowStep step1 = new WorkflowStep();
        step1.setId(step1Id);
        step1.setStepOrder(1);
        step1.setName("Lead Approval");
        step1.setApproverUserId(userId);
        step1.setStatusOnRejection("REJECTED");

        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setRecordData(new HashMap<>());

        when(workflowInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(workflowStepRepository.findById(step1Id)).thenReturn(Optional.of(step1));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(workflowInstanceRepository.save(any(WorkflowInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowDecisionRequest req = new WorkflowDecisionRequest();
        req.setAction(WorkflowActionType.REJECTED);
        req.setComments("Budget rejected due to cost overrun.");

        WorkflowInstanceResponse res = executionService.executeDecision(tenantId, instanceId, req, userId, false);

        assertNotNull(res);
        assertEquals(WorkflowInstanceStatus.REJECTED, res.getStatus());
        assertNotNull(res.getCompletedAt());
        assertEquals("REJECTED", record.getRecordData().get("workflow_status"));
        verify(workflowActionRepository, times(1)).save(any(WorkflowAction.class));
    }
}
