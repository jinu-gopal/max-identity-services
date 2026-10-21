package com.max.erp.identity.repository;

import com.max.erp.identity.entity.WorkflowAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowActionRepository extends JpaRepository<WorkflowAction, UUID> {

    List<WorkflowAction> findByInstanceIdOrderByActionTimestampAsc(UUID instanceId);
}
