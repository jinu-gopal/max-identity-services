package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectFieldOptionResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldOption;
import com.max.erp.identity.repository.BusinessObjectFieldOptionRepository;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class BusinessObjectFieldOptionService {

    private final BusinessObjectFieldOptionRepository optionRepository;
    private final BusinessObjectFieldRepository fieldRepository;

    private static final Set<String> OPTION_FIELD_TYPES = Set.of(
            "CHOICE",
            "MULTI_CHOICE"
    );

    public BusinessObjectFieldOptionService(
            BusinessObjectFieldOptionRepository optionRepository,
            BusinessObjectFieldRepository fieldRepository) {

        this.optionRepository = optionRepository;
        this.fieldRepository = fieldRepository;
    }

    // ============================================================
    // GET ALL OPTIONS
    // ============================================================

    @Transactional(readOnly = true)
    public List<BusinessObjectFieldOptionResponse> getOptions(
            UUID businessObjectId,
            UUID fieldId) {

        BusinessObjectField field =
                getValidatedField(businessObjectId, fieldId);

        return optionRepository
                .findAllByFieldIdOrderByDisplayOrderAsc(field.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // CREATE OPTION
    // ============================================================

    public BusinessObjectFieldOptionResponse createOption(
            UUID businessObjectId,
            UUID fieldId,
            CreateBusinessObjectFieldOptionRequest request) {

        BusinessObjectField field =
                getValidatedField(businessObjectId, fieldId);

        validateDraft(field.getBusinessObject());

        String optionValue =
                request.optionValue().trim();

        String optionLabel =
                request.optionLabel().trim();

        if (optionValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Option value cannot be blank");
        }

        if (optionLabel.isBlank()) {
            throw new IllegalArgumentException(
                    "Option label cannot be blank");
        }

        if (optionRepository.existsByFieldIdAndOptionValue(
                fieldId,
                optionValue)) {

            throw new IllegalArgumentException(
                    "An option with value '" +
                            optionValue +
                            "' already exists");
        }

        if (request.defaultOption() && !request.active()) {
            throw new IllegalArgumentException(
                    "An inactive option cannot be the default");
        }

        if (request.defaultOption()) {
            clearExistingDefault(fieldId);
        }

        BusinessObjectFieldOption option =
                new BusinessObjectFieldOption();

        option.setField(field);
        option.setOptionValue(optionValue);
        option.setOptionLabel(optionLabel);
        option.setDisplayOrder(request.displayOrder());
        option.setActive(request.active());
        option.setDefaultOption(request.defaultOption());

        BusinessObjectFieldOption savedOption =
                optionRepository.save(option);

        return toResponse(savedOption);
    }

    // ============================================================
    // UPDATE OPTION
    // ============================================================

    public BusinessObjectFieldOptionResponse updateOption(
            UUID businessObjectId,
            UUID fieldId,
            UUID optionId,
            UpdateBusinessObjectFieldOptionRequest request) {

        BusinessObjectField field =
                getValidatedField(businessObjectId, fieldId);

        validateDraft(field.getBusinessObject());

        BusinessObjectFieldOption option =
                optionRepository.findById(optionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Business object field option not found"));

        if (!option.getField()
                .getId()
                .equals(fieldId)) {

            throw new IllegalArgumentException(
                    "Option does not belong to the specified field");
        }

        // --------------------------------------------------------
        // OPTION VALUE
        // --------------------------------------------------------

        if (request.getOptionValue() != null) {

            String optionValue =
                    request.getOptionValue().trim();

            if (optionValue.isBlank()) {
                throw new IllegalArgumentException(
                        "Option value cannot be blank");
            }

            if (!optionValue.equals(option.getOptionValue())
                    && optionRepository.existsByFieldIdAndOptionValue(
                    fieldId,
                    optionValue)) {

                throw new IllegalArgumentException(
                        "An option with value '" +
                                optionValue +
                                "' already exists");
            }

            option.setOptionValue(optionValue);
        }

        // --------------------------------------------------------
        // OPTION LABEL
        // --------------------------------------------------------

        if (request.getOptionLabel() != null) {

            String optionLabel =
                    request.getOptionLabel().trim();

            if (optionLabel.isBlank()) {
                throw new IllegalArgumentException(
                        "Option label cannot be blank");
            }

            option.setOptionLabel(optionLabel);
        }

        // --------------------------------------------------------
        // DISPLAY ORDER
        // --------------------------------------------------------

        if (request.getDisplayOrder() != null) {
            option.setDisplayOrder(
                    request.getDisplayOrder());
        }

        // --------------------------------------------------------
        // ACTIVE
        // --------------------------------------------------------

        if (request.getActive() != null) {

            boolean active =
                    request.getActive();

            if (!active && option.isDefaultOption()) {
                throw new IllegalArgumentException(
                        "The default option cannot be inactive");
            }

            option.setActive(active);
        }

        // --------------------------------------------------------
        // DEFAULT
        // --------------------------------------------------------

        if (request.getDefaultOption() != null) {

            boolean defaultOption =
                    request.getDefaultOption();

            if (defaultOption && !option.isActive()) {
                throw new IllegalArgumentException(
                        "An inactive option cannot be the default");
            }

            if (defaultOption) {
                clearExistingDefault(fieldId);
            }

            option.setDefaultOption(defaultOption);
        }

        BusinessObjectFieldOption savedOption =
                optionRepository.save(option);

        return toResponse(savedOption);
    }

    // ============================================================
    // DELETE OPTION
    // ============================================================

    public void deleteOption(
            UUID businessObjectId,
            UUID fieldId,
            UUID optionId) {

        BusinessObjectField field =
                getValidatedField(businessObjectId, fieldId);

        validateDraft(field.getBusinessObject());

        BusinessObjectFieldOption option =
                optionRepository.findById(optionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Business object field option not found"));

        if (!option.getField()
                .getId()
                .equals(fieldId)) {

            throw new IllegalArgumentException(
                    "Option does not belong to the specified field");
        }

        optionRepository.delete(option);
    }

    // ============================================================
    // VALIDATE FIELD
    // ============================================================

    private BusinessObjectField getValidatedField(
            UUID businessObjectId,
            UUID fieldId) {

        BusinessObjectField field =
                fieldRepository.findById(fieldId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Business object field not found"));

        if (!field.getBusinessObject()
                .getId()
                .equals(businessObjectId)) {

            throw new IllegalArgumentException(
                    "Field does not belong to the specified business object");
        }

        String fieldType =
                field.getFieldType();

        if (!OPTION_FIELD_TYPES.contains(fieldType)) {

            throw new IllegalArgumentException(
                    "Options are only supported for CHOICE and MULTI_CHOICE fields");
        }

        return field;
    }

    // ============================================================
    // VALIDATE DRAFT
    // ============================================================

    private void validateDraft(
            BusinessObject businessObject) {

        if (!"DRAFT".equalsIgnoreCase(
                businessObject.getStatus())) {

            throw new IllegalStateException(
                    "Field options can only be modified while the business object is in DRAFT status");
        }
    }

    // ============================================================
    // CLEAR EXISTING DEFAULT
    // ============================================================

    private void clearExistingDefault(
            UUID fieldId) {

        optionRepository
                .findAllByFieldIdOrderByDisplayOrderAsc(fieldId)
                .forEach(option -> {

                    if (option.isDefaultOption()) {
                        option.setDefaultOption(false);
                    }
                });
    }

    // ============================================================
    // CONVERT ENTITY TO RESPONSE
    // ============================================================

    private BusinessObjectFieldOptionResponse toResponse(
            BusinessObjectFieldOption option) {

        return new BusinessObjectFieldOptionResponse(
                option.getId(),
                option.getField().getId(),
                option.getOptionValue(),
                option.getOptionLabel(),
                option.getDisplayOrder(),
                option.isActive(),
                option.isDefaultOption(),
                option.getCreatedAt(),
                option.getUpdatedAt()
        );
    }
}