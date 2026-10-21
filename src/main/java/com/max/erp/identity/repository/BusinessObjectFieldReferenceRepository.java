package com.max.erp.identity.repository;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessObjectFieldReferenceRepository
        extends JpaRepository<BusinessObjectFieldReference, UUID> {

    Optional<BusinessObjectFieldReference> findByField(BusinessObjectField field);

    Optional<BusinessObjectFieldReference> findByFieldId(UUID fieldId);

    boolean existsByFieldId(UUID fieldId);
}