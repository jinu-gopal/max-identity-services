package com.max.erp.identity.repository;

import com.max.erp.identity.entity.ConfigurationImportError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConfigurationImportErrorRepository
        extends JpaRepository<ConfigurationImportError, UUID> {

    List<ConfigurationImportError> findAllByImportJobIdOrderByRowNumberAsc(
            UUID importJobId
    );

    List<ConfigurationImportError> findAllByImportRowIdOrderByCreatedAtAsc(
            UUID importRowId
    );

    long countByImportJobId(UUID importJobId);
}
