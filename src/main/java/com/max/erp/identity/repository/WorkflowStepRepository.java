package com.max.erp.identity.repository;

import com.max.erp.identity.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    List<WorkflowStep> findByWorkflowIdOrderByStepOrderAsc(UUID workflowId);

    Optional<WorkflowStep> findByWorkflowIdAndStepOrder(
            UUID workflowId,
            Integer stepOrder
    );

    void deleteByWorkflowId(UUID workflowId);
}
