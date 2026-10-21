package com.max.erp.identity.repository;

import com.max.erp.identity.entity.WorkflowInstance;
import com.max.erp.identity.enums.WorkflowInstanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {

    List<WorkflowInstance> findByTenantIdOrderByInitiatedAtDesc(UUID tenantId);

    List<WorkflowInstance> findByTenantIdAndStatusOrderByInitiatedAtDesc(
            UUID tenantId,
            WorkflowInstanceStatus status
    );

    List<WorkflowInstance> findByTenantIdAndRecordIdOrderByInitiatedAtDesc(
            UUID tenantId,
            UUID recordId
    );

    Optional<WorkflowInstance> findFirstByRecordIdAndStatusOrderByInitiatedAtDesc(
            UUID recordId,
            WorkflowInstanceStatus status
    );

    @Query("SELECT wi FROM WorkflowInstance wi " +
           "JOIN WorkflowStep ws ON wi.currentStepId = ws.id " +
           "WHERE wi.tenantId = :tenantId " +
           "AND wi.status = 'PENDING' " +
           "AND (ws.approverUserId = :userId OR ws.approverRoleId IN :roleIds)")
    List<WorkflowInstance> findPendingForUserOrRoles(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId,
            @Param("roleIds") Collection<UUID> roleIds
    );

    @Query("SELECT wi FROM WorkflowInstance wi " +
           "JOIN WorkflowStep ws ON wi.currentStepId = ws.id " +
           "WHERE wi.tenantId = :tenantId " +
           "AND wi.status = 'PENDING' " +
           "AND (ws.approverUserId = :userId)")
    List<WorkflowInstance> findPendingForUserOnly(
            @Param("tenantId") UUID tenantId,
            @Param("userId") UUID userId
    );
}
