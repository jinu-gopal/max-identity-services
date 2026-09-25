package com.max.erp.identity.repository;

import com.max.erp.identity.entity.BusinessObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessObjectRepository
        extends JpaRepository<BusinessObject, UUID> {

    boolean existsByObjectKey(String objectKey);

    List<BusinessObject> findAllByOrderByNameAsc();
}