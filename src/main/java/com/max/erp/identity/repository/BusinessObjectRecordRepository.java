package com.max.erp.identity.repository;

import com.max.erp.identity.entity.BusinessObjectRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessObjectRecordRepository
        extends JpaRepository<BusinessObjectRecord, UUID> {

    List<BusinessObjectRecord> findByBusinessObjectIdOrderByCreatedAtDesc(
            UUID businessObjectId
    );
}