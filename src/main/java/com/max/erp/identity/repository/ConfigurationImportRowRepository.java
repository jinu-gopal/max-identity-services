package com.max.erp.identity.repository;

import com.max.erp.identity.entity.ConfigurationImportRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConfigurationImportRowRepository
        extends JpaRepository<ConfigurationImportRow, UUID> {

    List<ConfigurationImportRow> findAllByImportJobIdOrderByRowNumberAsc(
            UUID importJobId
    );

    long countByImportJobId(UUID importJobId);
}
