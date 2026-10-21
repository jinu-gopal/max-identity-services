package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    List<Workflow> findByTenantIdOrderByNameAsc(UUID tenantId);

    List<Workflow> findByTenantIdAndBusinessObjectIdOrderByNameAsc(
            UUID tenantId,
            UUID businessObjectId
    );

    Optional<Workflow> findByTenantIdAndWorkflowKey(
            UUID tenantId,
            String workflowKey
    );

    boolean existsByTenantIdAndWorkflowKey(
            UUID tenantId,
            String workflowKey
    );
}
