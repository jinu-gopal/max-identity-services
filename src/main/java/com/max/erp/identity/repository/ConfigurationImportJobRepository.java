package com.max.erp.identity.repository;

import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.enums.ImportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConfigurationImportJobRepository
        extends JpaRepository<ConfigurationImportJob, UUID> {

    List<ConfigurationImportJob> findAllByBusinessObjectIdOrderByCreatedAtDesc(
            UUID businessObjectId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ConfigurationImportJob j SET j.status = :newStatus, j.startedAt = :startedAt, j.updatedAt = :now WHERE j.id = :jobId AND j.status = :expectedStatus")
    int updateStatusIfStatus(
            @Param("jobId") UUID jobId,
            @Param("newStatus") ImportJobStatus newStatus,
            @Param("startedAt") Instant startedAt,
            @Param("now") Instant now,
            @Param("expectedStatus") ImportJobStatus expectedStatus
    );
}
