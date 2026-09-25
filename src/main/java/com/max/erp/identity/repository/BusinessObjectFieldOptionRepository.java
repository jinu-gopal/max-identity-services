package com.max.erp.identity.repository;

import com.max.erp.identity.entity.BusinessObjectFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessObjectFieldOptionRepository
        extends JpaRepository<BusinessObjectFieldOption, UUID> {

    List<BusinessObjectFieldOption> findAllByFieldIdOrderByDisplayOrderAsc(
            UUID fieldId
    );

    boolean existsByFieldIdAndOptionValue(
            UUID fieldId,
            String optionValue
    );
}