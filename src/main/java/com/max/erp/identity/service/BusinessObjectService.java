package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectResponse;
import com.max.erp.identity.dto.CreateBusinessObjectRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.repository.BusinessObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BusinessObjectService {

    private final BusinessObjectRepository businessObjectRepository;

    public BusinessObjectService(
            BusinessObjectRepository businessObjectRepository) {

        this.businessObjectRepository = businessObjectRepository;
    }

    /**
     * Get all business objects.
     */
    @Transactional(readOnly = true)
    public List<BusinessObjectResponse> getAll() {

        return businessObjectRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Create a new business object.
     *
     * New objects always start as DRAFT version 1.
     */
    public BusinessObjectResponse create(
            CreateBusinessObjectRequest request) {

        if (businessObjectRepository.existsByObjectKey(
                request.getObjectKey())) {

            throw new IllegalArgumentException(
                    "Business object key already exists"
            );
        }

        BusinessObject businessObject = new BusinessObject();

        businessObject.setObjectKey(request.getObjectKey());
        businessObject.setName(request.getName());
        businessObject.setDescription(request.getDescription());
        businessObject.setStatus("DRAFT");
        businessObject.setVersionNumber(1);

        BusinessObject saved =
                businessObjectRepository.save(businessObject);

        return toResponse(saved);
    }

    /**
     * Update a business object.
     *
     * Only DRAFT business objects can currently be updated.
     * The objectKey is intentionally immutable.
     */
    public BusinessObjectResponse update(
            UUID id,
            UpdateBusinessObjectRequest request) {

        BusinessObject businessObject =
                businessObjectRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Business object not found"
                                ));

        if (!"DRAFT".equals(businessObject.getStatus())) {
            throw new IllegalStateException(
                    "Only DRAFT business objects can be updated"
            );
        }

        businessObject.setName(request.getName());
        businessObject.setDescription(request.getDescription());

        BusinessObject saved =
                businessObjectRepository.save(businessObject);

        return toResponse(saved);
    }

    /**
     * Convert entity to API response.
     *
     * Internal database fields such as password information
     * are never exposed through this response.
     */
    private BusinessObjectResponse toResponse(
            BusinessObject businessObject) {

        BusinessObjectResponse response =
                new BusinessObjectResponse();

        response.setId(businessObject.getId());
        response.setObjectKey(businessObject.getObjectKey());
        response.setName(businessObject.getName());
        response.setDescription(businessObject.getDescription());
        response.setStatus(businessObject.getStatus());
        response.setVersionNumber(
                businessObject.getVersionNumber()
        );
        response.setCreatedBy(businessObject.getCreatedBy());
        response.setCreatedAt(businessObject.getCreatedAt());
        response.setUpdatedAt(businessObject.getUpdatedAt());

        return response;
    }
}