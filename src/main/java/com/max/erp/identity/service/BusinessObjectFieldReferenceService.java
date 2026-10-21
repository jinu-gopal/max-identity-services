package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectFieldReferenceResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldReference;
import com.max.erp.identity.repository.BusinessObjectFieldReferenceRepository;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional
public class BusinessObjectFieldReferenceService {

    private final BusinessObjectFieldReferenceRepository referenceRepository;
    private final BusinessObjectFieldRepository fieldRepository;
    private final BusinessObjectRepository businessObjectRepository;

    public BusinessObjectFieldReferenceService(
            BusinessObjectFieldReferenceRepository referenceRepository,
            BusinessObjectFieldRepository fieldRepository,
            BusinessObjectRepository businessObjectRepository
    ) {
        this.referenceRepository = referenceRepository;
        this.fieldRepository = fieldRepository;
        this.businessObjectRepository = businessObjectRepository;
    }

    @Transactional(readOnly = true)
    public BusinessObjectFieldReferenceResponse getReference(
            UUID businessObjectId,
            UUID fieldId
    ) {
        BusinessObjectField field = getField(businessObjectId, fieldId);

        return referenceRepository.findByFieldId(fieldId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reference configuration not found"
                ));
    }

    public BusinessObjectFieldReferenceResponse createReference(
            UUID businessObjectId,
            UUID fieldId,
            CreateBusinessObjectFieldReferenceRequest request
    ) {
        BusinessObjectField field = getField(businessObjectId, fieldId);
        validateDraft(field.getBusinessObject());
        validateReferenceField(field);

        if (referenceRepository.existsByFieldId(fieldId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reference configuration already exists for this field"
            );
        }

        BusinessObject targetObject = getBusinessObject(
                request.targetBusinessObjectId()
        );

        BusinessObjectField displayField = getDisplayField(
                targetObject,
                request.displayFieldId()
        );

        BusinessObjectFieldReference reference =
                new BusinessObjectFieldReference();

        reference.setField(field);
        reference.setTargetBusinessObject(targetObject);
        reference.setDisplayField(displayField);
        reference.setAllowMultiple(request.allowMultiple());
        reference.setAllowCreate(request.allowCreate());

        return toResponse(referenceRepository.save(reference));
    }

    public BusinessObjectFieldReferenceResponse updateReference(
            UUID businessObjectId,
            UUID fieldId,
            UUID referenceId,
            UpdateBusinessObjectFieldReferenceRequest request
    ) {
        BusinessObjectField field = getField(businessObjectId, fieldId);
        validateDraft(field.getBusinessObject());
        validateReferenceField(field);

        BusinessObjectFieldReference reference =
                referenceRepository.findById(referenceId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reference configuration not found"
                        ));

        if (!reference.getField().getId().equals(fieldId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reference configuration does not belong to this field"
            );
        }

        BusinessObject targetObject = getBusinessObject(
                request.targetBusinessObjectId()
        );

        BusinessObjectField displayField = getDisplayField(
                targetObject,
                request.displayFieldId()
        );

        reference.setTargetBusinessObject(targetObject);
        reference.setDisplayField(displayField);
        reference.setAllowMultiple(request.allowMultiple());
        reference.setAllowCreate(request.allowCreate());

        return toResponse(referenceRepository.save(reference));
    }

    public void deleteReference(
            UUID businessObjectId,
            UUID fieldId,
            UUID referenceId
    ) {
        BusinessObjectField field = getField(businessObjectId, fieldId);
        validateDraft(field.getBusinessObject());

        BusinessObjectFieldReference reference =
                referenceRepository.findById(referenceId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reference configuration not found"
                        ));

        if (!reference.getField().getId().equals(fieldId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reference configuration does not belong to this field"
            );
        }

        referenceRepository.delete(reference);
    }

    private BusinessObjectField getField(
            UUID businessObjectId,
            UUID fieldId
    ) {
        BusinessObjectField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business object field not found"
                ));

        if (!field.getBusinessObject().getId().equals(businessObjectId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Field does not belong to the specified business object"
            );
        }

        return field;
    }

    private BusinessObject getBusinessObject(UUID businessObjectId) {
        return businessObjectRepository.findById(businessObjectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Target business object not found"
                ));
    }

    private BusinessObjectField getDisplayField(
            BusinessObject targetObject,
            UUID displayFieldId
    ) {
        if (displayFieldId == null) {
            return null;
        }

        BusinessObjectField displayField =
                fieldRepository.findById(displayFieldId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Display field not found"
                        ));

        if (!displayField.getBusinessObject().getId()
                .equals(targetObject.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Display field must belong to the target business object"
            );
        }

        return displayField;
    }

    private void validateReferenceField(BusinessObjectField field) {
        if (!"REFERENCE".equalsIgnoreCase(field.getFieldType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reference configuration is only supported for REFERENCE fields"
            );
        }
    }

    private void validateDraft(BusinessObject businessObject) {
        if (!"DRAFT".equalsIgnoreCase(businessObject.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reference configuration can only be changed while the business object is DRAFT"
            );
        }
    }

    private BusinessObjectFieldReferenceResponse toResponse(
            BusinessObjectFieldReference reference
    ) {
        return new BusinessObjectFieldReferenceResponse(
                reference.getId(),
                reference.getField().getId(),
                reference.getTargetBusinessObject().getId(),
                reference.getDisplayField() != null
                        ? reference.getDisplayField().getId()
                        : null,
                reference.isAllowMultiple(),
                reference.isAllowCreate()
        );
    }
}