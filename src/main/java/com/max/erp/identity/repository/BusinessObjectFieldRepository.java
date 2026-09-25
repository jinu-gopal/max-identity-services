package com.max.erp.identity.repository;

import com.max.erp.identity.entity.BusinessObjectField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessObjectFieldRepository
        extends JpaRepository<BusinessObjectField, UUID> {

    boolean existsByBusinessObjectIdAndFieldKey(
            UUID businessObjectId,
            String fieldKey
    );

    List<BusinessObjectField> findAllByBusinessObjectIdOrderByDisplayOrderAsc(
            UUID businessObjectId
    );
}