package com.max.erp.identity.validation;

import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldOption;
import com.max.erp.identity.entity.BusinessObjectFieldReference;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.exception.ApiErrorResponse;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.repository.BusinessObjectFieldOptionRepository;
import com.max.erp.identity.repository.BusinessObjectFieldReferenceRepository;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.UserRepository;
import com.max.erp.identity.service.BusinessObjectRecordService;
import com.max.erp.identity.validation.validator.ChoiceFieldValidator;
import com.max.erp.identity.validation.validator.DataTypeValidator;
import com.max.erp.identity.validation.validator.NumericRangeValidator;
import com.max.erp.identity.validation.validator.PatternAndFormatValidator;
import com.max.erp.identity.validation.validator.ReferenceFieldValidator;
import com.max.erp.identity.validation.validator.RequiredFieldValidator;
import com.max.erp.identity.validation.validator.RecordValidator;
import com.max.erp.identity.validation.validator.UniqueValueValidator;
import com.max.erp.identity.validation.validator.UserFieldValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class RecordValidationEngineTest {

    private final RequiredFieldValidator requiredFieldValidator =
            new RequiredFieldValidator();

    private final DataTypeValidator dataTypeValidator =
            new DataTypeValidator();

    private final BusinessObjectFieldOptionRepository optionRepository =
            mock(BusinessObjectFieldOptionRepository.class);

    private final ChoiceFieldValidator choiceFieldValidator =
            new ChoiceFieldValidator(optionRepository);

    private final NumericRangeValidator numericRangeValidator =
            new NumericRangeValidator();

    private final PatternAndFormatValidator patternAndFormatValidator =
            new PatternAndFormatValidator();

    private final BusinessObjectFieldReferenceRepository referenceRepository =
            mock(BusinessObjectFieldReferenceRepository.class);

    private final BusinessObjectRecordRepository referenceRecordRepository =
            mock(BusinessObjectRecordRepository.class);

    private final ReferenceFieldValidator referenceFieldValidator =
            new ReferenceFieldValidator(referenceRepository, referenceRecordRepository);

    private final UserRepository userRepository =
            mock(UserRepository.class);

    private final UserFieldValidator userFieldValidator =
            new UserFieldValidator(userRepository);

    private final UniqueValueValidator uniqueValueValidator =
            new UniqueValueValidator(referenceRecordRepository);

    private final RecordValidationEngine validationEngine =
            new RecordValidationEngineImpl(
                    List.of(
                            requiredFieldValidator,
                            dataTypeValidator,
                            choiceFieldValidator,
                            numericRangeValidator,
                            patternAndFormatValidator,
                            referenceFieldValidator,
                            userFieldValidator,
                            uniqueValueValidator
                    )
            );

    @Test
    void requiredFieldShouldFailWhenValueIsMissing() {
        BusinessObjectField field =
                field("company_name", "TEXT", true);

        ValidationContext context = context(
                List.of(field),
                Map.of()
        );

        ValidationResult result =
                requiredFieldValidator.validate(context).isEmpty()
                        ? ValidationResult.success()
                        : ValidationResult.failure(
                                requiredFieldValidator.validate(context)
                        );

        assertFalse(result.valid());
        assertEquals("REQUIRED", result.errors().get(0).code());
        assertEquals("company_name", result.errors().get(0).fieldKey());
    }

    @Test
    void requiredFieldShouldFailWhenValueIsBlank() {
        BusinessObjectField field =
                field("company_name", "TEXT", true);

        ValidationContext context = context(
                List.of(field),
                Map.of("company_name", "   ")
        );

        List<ValidationError> errors =
                requiredFieldValidator.validate(context);

        assertEquals(1, errors.size());
        assertEquals("REQUIRED", errors.get(0).code());
    }

    @Test
    void optionalFieldShouldAllowMissingValue() {
        BusinessObjectField field =
                field("description", "TEXT", false);

        ValidationContext context = context(
                List.of(field),
                Map.of()
        );

        List<ValidationError> errors =
                requiredFieldValidator.validate(context);

        assertTrue(errors.isEmpty());
    }

    @Test
    void textFieldShouldAcceptString() {
        BusinessObjectField field =
                field("company_name", "TEXT", false);

        ValidationContext context = context(
                List.of(field),
                Map.of("company_name", "MAX ERP")
        );

        List<ValidationError> errors =
                dataTypeValidator.validate(context);

        assertTrue(errors.isEmpty());
    }

    @Test
    void numberFieldShouldAcceptNumber() {
        BusinessObjectField field =
                field("employee_count", "NUMBER", false);

        ValidationContext context = context(
                List.of(field),
                Map.of("employee_count", 100)
        );

        List<ValidationError> errors =
                dataTypeValidator.validate(context);

        assertTrue(errors.isEmpty());
    }

    @Test
    void numberFieldShouldRejectString() {
        BusinessObjectField field =
                field("employee_count", "NUMBER", false);

        ValidationContext context = context(
                List.of(field),
                Map.of("employee_count", "100")
        );

        List<ValidationError> errors =
                dataTypeValidator.validate(context);

        assertEquals(1, errors.size());
        assertEquals("INVALID_TYPE", errors.get(0).code());
        assertEquals("employee_count", errors.get(0).fieldKey());
    }

    @Test
    void numericFieldTypesShouldAcceptNumbersAndRejectStrings() {
        List<BusinessObjectField> fields = List.of(
                field("unit_price", "DECIMAL", false),
                field("annual_revenue", "CURRENCY", false),
                field("discount_rate", "PERCENTAGE", false)
        );

        ValidationContext validContext = context(
                fields,
                Map.of(
                        "unit_price", new BigDecimal("19.99"),
                        "annual_revenue", 250000.50,
                        "discount_rate", 12.5
                )
        );

        assertTrue(dataTypeValidator.validate(validContext).isEmpty());

        ValidationContext invalidContext = context(
                fields,
                Map.of(
                        "unit_price", "19.99",
                        "annual_revenue", "250000.50",
                        "discount_rate", "12.5"
                )
        );

        List<ValidationError> errors = dataTypeValidator.validate(invalidContext);
        assertEquals(3, errors.size());
        assertTrue(errors.stream().allMatch(e -> "INVALID_TYPE".equals(e.code())));
    }

    @Test
    void booleanFieldShouldAcceptBoolean() {
        BusinessObjectField field =
                field("active", "BOOLEAN", false);

        ValidationContext context = context(
                List.of(field),
                Map.of("active", true)
        );

        List<ValidationError> errors =
                dataTypeValidator.validate(context);

        assertTrue(errors.isEmpty());
    }

    @Test
    void multiChoiceShouldAcceptCollection() {
        BusinessObjectField field =
                field("customer_types", "MULTI_CHOICE", false);

        ValidationContext context = context(
                List.of(field),
                Map.of(
                        "customer_types",
                        List.of("individual", "corporate")
                )
        );

        List<ValidationError> errors =
                dataTypeValidator.validate(context);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validationEngineShouldReturnValidForValidRecord() {
        BusinessObjectField companyName =
                field("company_name", "TEXT", true);

        BusinessObjectField employeeCount =
                field("employee_count", "NUMBER", false);

        ValidationContext context = context(
                List.of(companyName, employeeCount),
                Map.of(
                        "company_name", "MAX ERP",
                        "employee_count", 100
                )
        );

        ValidationResult result =
                validationEngine.validate(context);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validationEngineShouldReturnErrorsForInvalidRecord() {
        BusinessObjectField companyName =
                field("company_name", "TEXT", true);

        BusinessObjectField employeeCount =
                field("employee_count", "NUMBER", false);

        ValidationContext context = context(
                List.of(companyName, employeeCount),
                Map.of(
                        "company_name", "",
                        "employee_count", "100"
                )
        );

        ValidationResult result =
                validationEngine.validate(context);

        assertFalse(result.valid());
        assertEquals(2, result.errors().size());
    }

    @Test
    void createRecordShouldPersistWhenValidationSucceeds() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);
        RecordValidationEngine spyValidationEngine =
                spy(validationEngine);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                spyValidationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        BusinessObject businessObject = new BusinessObject();
        List<BusinessObjectField> fields = List.of(
                field("company_name", "TEXT", true),
                field("employee_count", "NUMBER", false)
        );

        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(fields);
        when(recordRepository.save(any(BusinessObjectRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> recordData = Map.of(
                "company_name", "MAX ERP",
                "employee_count", 100
        );

        BusinessObjectRecord saved = service.createRecord(
                businessObjectId,
                recordData,
                createdBy
        );

        ArgumentCaptor<ValidationContext> contextCaptor =
                ArgumentCaptor.forClass(ValidationContext.class);
        verify(spyValidationEngine, times(1)).validate(contextCaptor.capture());

        ValidationContext capturedContext = contextCaptor.getValue();
        assertSame(businessObject, capturedContext.businessObject());
        assertEquals(fields, capturedContext.fields());
        assertEquals(ValidationOperation.CREATE, capturedContext.operation());
        assertEquals(recordData, capturedContext.values());

        assertNotNull(saved.getId());
        assertEquals(businessObjectId, saved.getBusinessObjectId());
        assertEquals("MAX ERP", saved.getRecordData().get("company_name"));
        assertEquals(100, saved.getRecordData().get("employee_count"));
        verify(recordRepository, times(1)).save(any(BusinessObjectRecord.class));
    }

    @Test
    void createRecordShouldRejectMissingRequiredFieldAndInvalidDataType() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        BusinessObject businessObject = new BusinessObject();

        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(
                        field("company_name", "TEXT", true),
                        field("employee_count", "NUMBER", false)
                ));

        RecordValidationException exception = assertThrows(
                RecordValidationException.class,
                () -> service.createRecord(
                        businessObjectId,
                        Map.of("employee_count", "invalid-number"),
                        UUID.randomUUID()
                )
        );

        assertEquals(2, exception.getErrors().size());
        assertEquals("company_name", exception.getErrors().get(0).fieldKey());
        assertEquals("REQUIRED", exception.getErrors().get(0).code());
        assertEquals("employee_count", exception.getErrors().get(1).fieldKey());
        assertEquals("INVALID_TYPE", exception.getErrors().get(1).code());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void updateRecordShouldPreserveExistingFieldsAndValidateMergedData() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);
        RecordValidationEngine spyValidationEngine =
                spy(validationEngine);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                spyValidationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        BusinessObject businessObject = new BusinessObject();
        List<BusinessObjectField> fields = List.of(
                field("company_name", "TEXT", true),
                field("employee_count", "NUMBER", false)
        );

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(recordId);
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(new LinkedHashMap<>(Map.of(
                "company_name", "MAX ERP",
                "employee_count", 50
        )));

        when(recordRepository.findById(recordId))
                .thenReturn(Optional.of(existingRecord));
        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(fields);
        when(recordRepository.save(any(BusinessObjectRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessObjectRecord updated = service.updateRecord(
                businessObjectId,
                recordId,
                Map.of("employee_count", 120)
        );

        ArgumentCaptor<ValidationContext> contextCaptor =
                ArgumentCaptor.forClass(ValidationContext.class);
        verify(spyValidationEngine, times(1)).validate(contextCaptor.capture());

        ValidationContext capturedContext = contextCaptor.getValue();
        assertSame(businessObject, capturedContext.businessObject());
        assertEquals(fields, capturedContext.fields());
        assertEquals(ValidationOperation.UPDATE, capturedContext.operation());
        assertEquals("MAX ERP", capturedContext.values().get("company_name"));
        assertEquals(120, capturedContext.values().get("employee_count"));

        assertEquals("MAX ERP", updated.getRecordData().get("company_name"));
        assertEquals(120, updated.getRecordData().get("employee_count"));
        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    void updateRecordShouldFailWhenUpdatedDataViolatesValidation() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        BusinessObject businessObject = new BusinessObject();

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(recordId);
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(new LinkedHashMap<>(Map.of(
                "company_name", "MAX ERP",
                "employee_count", 50
        )));

        when(recordRepository.findById(recordId))
                .thenReturn(Optional.of(existingRecord));
        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(
                        field("company_name", "TEXT", true),
                        field("employee_count", "NUMBER", false)
                ));

        RecordValidationException exception = assertThrows(
                RecordValidationException.class,
                () -> service.updateRecord(
                        businessObjectId,
                        recordId,
                        Map.of(
                                "company_name", "   ",
                                "employee_count", "not-a-number"
                        )
                )
        );

        assertEquals(2, exception.getErrors().size());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void globalExceptionHandlerShouldReturnHttp400WithFieldErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI())
                .thenReturn("/api/v1/configuration/business-objects/123/records");

        RecordValidationException exception = new RecordValidationException(
                List.of(
                        new ValidationError(
                                "company_name",
                                "REQUIRED",
                                "Field is required"
                        ),
                        new ValidationError(
                                "employee_count",
                                "INVALID_TYPE",
                                "Value does not match field type NUMBER"
                        )
                )
        );

        ResponseEntity<ApiErrorResponse> responseEntity =
                handler.handleRecordValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ApiErrorResponse body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("VALIDATION_ERROR", body.code());
        assertEquals("Field is required", body.fieldErrors().get("company_name"));
        assertEquals(
                "Value does not match field type NUMBER",
                body.fieldErrors().get("employee_count")
        );
    }

    @Test
    void choiceFieldShouldAcceptValidActiveOption() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField tierField = field("customer_tier", "CHOICE", true);
        tierField.setId(fieldId);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(
                        option("standard", "Standard", true),
                        option("enterprise", "Enterprise", true)
                ));

        ValidationResult result = validationEngine.validate(
                context(List.of(tierField), Map.of("customer_tier", "enterprise"))
        );

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void choiceFieldShouldRejectInvalidOrUnknownOption() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField tierField = field("customer_tier", "CHOICE", true);
        tierField.setId(fieldId);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(
                        option("standard", "Standard", true),
                        option("enterprise", "Enterprise", true)
                ));

        ValidationResult result = validationEngine.validate(
                context(List.of(tierField), Map.of("customer_tier", "platinum"))
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("customer_tier", result.errors().get(0).fieldKey());
        assertEquals("INVALID_CHOICE", result.errors().get(0).code());
    }

    @Test
    void choiceFieldShouldRejectInactiveOption() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField tierField = field("customer_tier", "CHOICE", false);
        tierField.setId(fieldId);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(
                        option("standard", "Standard", true),
                        option("legacy", "Legacy", false)
                ));

        ValidationResult result = validationEngine.validate(
                context(List.of(tierField), Map.of("customer_tier", "legacy"))
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("customer_tier", result.errors().get(0).fieldKey());
        assertEquals("INVALID_CHOICE", result.errors().get(0).code());
    }

    @Test
    void optionalChoiceAndMultiChoiceShouldAllowEmptyValues() {
        UUID choiceFieldId = UUID.randomUUID();
        BusinessObjectField choiceField = field("customer_tier", "CHOICE", false);
        choiceField.setId(choiceFieldId);

        UUID multiChoiceFieldId = UUID.randomUUID();
        BusinessObjectField multiChoiceField = field("customer_types", "MULTI_CHOICE", false);
        multiChoiceField.setId(multiChoiceFieldId);

        ValidationResult missingValuesResult = validationEngine.validate(
                context(List.of(choiceField, multiChoiceField), Map.of())
        );
        assertTrue(missingValuesResult.valid());

        ValidationResult blankAndEmptyCollectionResult = validationEngine.validate(
                context(
                        List.of(choiceField, multiChoiceField),
                        Map.of(
                                "customer_tier", "",
                                "customer_types", List.of()
                        )
                )
        );
        assertTrue(blankAndEmptyCollectionResult.valid());
        verify(optionRepository, never())
                .findAllByFieldIdOrderByDisplayOrderAsc(choiceFieldId);
        verify(optionRepository, never())
                .findAllByFieldIdOrderByDisplayOrderAsc(multiChoiceFieldId);
    }

    @Test
    void requiredMultiChoiceShouldFailWhenCollectionIsEmpty() {
        BusinessObjectField multiChoiceField =
                field("customer_types", "MULTI_CHOICE", true);

        ValidationResult result = validationEngine.validate(
                context(
                        List.of(multiChoiceField),
                        Map.of("customer_types", List.of())
                )
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("customer_types", result.errors().get(0).fieldKey());
        assertEquals("REQUIRED", result.errors().get(0).code());
    }

    @Test
    void multiChoiceShouldAcceptMultipleActiveOptionsWithSingleRepositoryQuery() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField multiChoiceField =
                field("customer_types", "MULTI_CHOICE", true);
        multiChoiceField.setId(fieldId);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(
                        option("individual", "Individual", true),
                        option("corporate", "Corporate", true),
                        option("government", "Government", true)
                ));

        ValidationResult result = validationEngine.validate(
                context(
                        List.of(multiChoiceField),
                        Map.of(
                                "customer_types",
                                List.of("individual", "corporate", "government")
                        )
                )
        );

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
        verify(optionRepository, times(1))
                .findAllByFieldIdOrderByDisplayOrderAsc(fieldId);
    }

    @Test
    void multiChoiceShouldRejectInvalidOrInactiveSelection() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField multiChoiceField =
                field("customer_types", "MULTI_CHOICE", false);
        multiChoiceField.setId(fieldId);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(
                        option("individual", "Individual", true),
                        option("archived", "Archived", false)
                ));

        ValidationResult inactiveResult = validationEngine.validate(
                context(
                        List.of(multiChoiceField),
                        Map.of("customer_types", List.of("individual", "archived"))
                )
        );

        assertFalse(inactiveResult.valid());
        assertEquals(1, inactiveResult.errors().size());
        assertEquals("customer_types", inactiveResult.errors().get(0).fieldKey());
        assertEquals("INVALID_MULTI_CHOICE", inactiveResult.errors().get(0).code());

        ValidationResult unknownResult = validationEngine.validate(
                context(
                        List.of(multiChoiceField),
                        Map.of("customer_types", List.of("individual", "unknown_type"))
                )
        );

        assertFalse(unknownResult.valid());
        assertEquals(1, unknownResult.errors().size());
        assertEquals("customer_types", unknownResult.errors().get(0).fieldKey());
        assertEquals("INVALID_MULTI_CHOICE", unknownResult.errors().get(0).code());
    }

    @Test
    void multiChoiceShouldRejectIncorrectTypes() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField multiChoiceField =
                field("customer_types", "MULTI_CHOICE", false);
        multiChoiceField.setId(fieldId);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(
                        option("individual", "Individual", true)
                ));

        ValidationResult stringInsteadOfCollectionResult = validationEngine.validate(
                context(
                        List.of(multiChoiceField),
                        Map.of("customer_types", "individual")
                )
        );

        assertFalse(stringInsteadOfCollectionResult.valid());
        assertEquals(1, stringInsteadOfCollectionResult.errors().size());
        assertEquals("customer_types", stringInsteadOfCollectionResult.errors().get(0).fieldKey());
        assertEquals("INVALID_TYPE", stringInsteadOfCollectionResult.errors().get(0).code());

        ValidationResult nonStringElementInCollectionResult = validationEngine.validate(
                context(
                        List.of(multiChoiceField),
                        Map.of("customer_types", List.of(123))
                )
        );

        assertFalse(nonStringElementInCollectionResult.valid());
        assertEquals(1, nonStringElementInCollectionResult.errors().size());
        assertEquals("customer_types", nonStringElementInCollectionResult.errors().get(0).fieldKey());
        assertEquals("INVALID_MULTI_CHOICE", nonStringElementInCollectionResult.errors().get(0).code());
    }

    @Test
    void numericRangeShouldAcceptExactMinAndMaxBoundaries() {
        BusinessObjectField countField = fieldWithRange(
                "employee_count",
                "NUMBER",
                true,
                new BigDecimal("10"),
                new BigDecimal("500")
        );

        ValidationResult minBoundaryResult = validationEngine.validate(
                context(List.of(countField), Map.of("employee_count", 10))
        );
        assertTrue(minBoundaryResult.valid());
        assertTrue(minBoundaryResult.errors().isEmpty());

        ValidationResult maxBoundaryResult = validationEngine.validate(
                context(List.of(countField), Map.of("employee_count", 500))
        );
        assertTrue(maxBoundaryResult.valid());
        assertTrue(maxBoundaryResult.errors().isEmpty());
    }

    @Test
    void numericRangeShouldRejectValueBelowMinimumAndAboveMaximum() {
        BusinessObjectField countField = fieldWithRange(
                "employee_count",
                "NUMBER",
                true,
                new BigDecimal("10"),
                new BigDecimal("500")
        );

        ValidationResult belowMinResult = validationEngine.validate(
                context(List.of(countField), Map.of("employee_count", 9))
        );
        assertFalse(belowMinResult.valid());
        assertEquals(1, belowMinResult.errors().size());
        assertEquals("employee_count", belowMinResult.errors().get(0).fieldKey());
        assertEquals("VALUE_BELOW_MINIMUM", belowMinResult.errors().get(0).code());

        ValidationResult aboveMaxResult = validationEngine.validate(
                context(List.of(countField), Map.of("employee_count", 501))
        );
        assertFalse(aboveMaxResult.valid());
        assertEquals(1, aboveMaxResult.errors().size());
        assertEquals("employee_count", aboveMaxResult.errors().get(0).fieldKey());
        assertEquals("VALUE_ABOVE_MAXIMUM", aboveMaxResult.errors().get(0).code());
    }

    @Test
    void numericRangeShouldSupportOnlyMinimumAndOnlyMaximumConfigurations() {
        BusinessObjectField onlyMinField = fieldWithRange(
                "min_order_qty",
                "NUMBER",
                false,
                new BigDecimal("1"),
                null
        );

        assertTrue(
                validationEngine.validate(
                        context(List.of(onlyMinField), Map.of("min_order_qty", 1000000))
                ).valid()
        );

        ValidationResult belowOnlyMin = validationEngine.validate(
                context(List.of(onlyMinField), Map.of("min_order_qty", 0))
        );
        assertFalse(belowOnlyMin.valid());
        assertEquals("VALUE_BELOW_MINIMUM", belowOnlyMin.errors().get(0).code());

        BusinessObjectField onlyMaxField = fieldWithRange(
                "discount_rate",
                "PERCENTAGE",
                false,
                null,
                new BigDecimal("50")
        );

        assertTrue(
                validationEngine.validate(
                        context(List.of(onlyMaxField), Map.of("discount_rate", -10))
                ).valid()
        );

        ValidationResult aboveOnlyMax = validationEngine.validate(
                context(List.of(onlyMaxField), Map.of("discount_rate", 50.01))
        );
        assertFalse(aboveOnlyMax.valid());
        assertEquals("VALUE_ABOVE_MAXIMUM", aboveOnlyMax.errors().get(0).code());
    }

    @Test
    void numericRangeShouldAllowAnyValidNumberWhenNoRangeConfiguredAndAllowOptionalNull() {
        BusinessObjectField unboundedField = field("employee_count", "NUMBER", false);

        assertTrue(
                validationEngine.validate(
                        context(List.of(unboundedField), Map.of("employee_count", -999999))
                ).valid()
        );

        BusinessObjectField optionalBoundedField = fieldWithRange(
                "employee_count",
                "NUMBER",
                false,
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        assertTrue(
                validationEngine.validate(
                        context(List.of(optionalBoundedField), Map.of())
                ).valid()
        );

        Map<String, Object> explicitNullMap = new LinkedHashMap<>();
        explicitNullMap.put("employee_count", null);
        assertTrue(
                validationEngine.validate(
                        context(List.of(optionalBoundedField), explicitNullMap)
                ).valid()
        );
    }

    @Test
    void numericRangeShouldSupportDecimalBoundaryComparisonsWithoutPrecisionErrors() {
        BusinessObjectField priceField = fieldWithRange(
                "unit_price",
                "CURRENCY",
                true,
                new BigDecimal("19.9900"),
                new BigDecimal("99.9500")
        );

        assertTrue(
                validationEngine.validate(
                        context(List.of(priceField), Map.of("unit_price", 19.99d))
                ).valid()
        );
        assertTrue(
                validationEngine.validate(
                        context(List.of(priceField), Map.of("unit_price", 99.95f))
                ).valid()
        );

        ValidationResult slightlyBelow = validationEngine.validate(
                context(List.of(priceField), Map.of("unit_price", 19.989d))
        );
        assertFalse(slightlyBelow.valid());
        assertEquals("VALUE_BELOW_MINIMUM", slightlyBelow.errors().get(0).code());

        ValidationResult slightlyAbove = validationEngine.validate(
                context(List.of(priceField), Map.of("unit_price", 99.951d))
        );
        assertFalse(slightlyAbove.valid());
        assertEquals("VALUE_ABOVE_MAXIMUM", slightlyAbove.errors().get(0).code());
    }

    @Test
    void numericRangeValidatorShouldReturnControlledErrorsForInvalidInputTypes() {
        BusinessObjectField boundedField = fieldWithRange(
                "unit_price",
                "DECIMAL",
                false,
                new BigDecimal("0"),
                new BigDecimal("100")
        );

        List<ValidationError> stringErrors = numericRangeValidator.validate(
                context(List.of(boundedField), Map.of("unit_price", "not-a-number"))
        );
        assertEquals(1, stringErrors.size());
        assertEquals("INVALID_TYPE", stringErrors.get(0).code());

        List<ValidationError> booleanErrors = numericRangeValidator.validate(
                context(List.of(boundedField), Map.of("unit_price", Boolean.TRUE))
        );
        assertEquals(1, booleanErrors.size());
        assertEquals("INVALID_TYPE", booleanErrors.get(0).code());

        ValidationResult nanEngineResult = validationEngine.validate(
                context(List.of(boundedField), Map.of("unit_price", Double.NaN))
        );
        assertFalse(nanEngineResult.valid());
        assertEquals(1, nanEngineResult.errors().size());
        assertEquals("INVALID_TYPE", nanEngineResult.errors().get(0).code());
    }

    @Test
    void createAndUpdateRecordShouldRejectOutOfRangeNumericValuesWithoutPersistence() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        BusinessObject businessObject = new BusinessObject();

        BusinessObjectField discountField = fieldWithRange(
                "discount_rate",
                "PERCENTAGE",
                true,
                new BigDecimal("0"),
                new BigDecimal("100")
        );

        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(discountField));

        RecordValidationException createEx = assertThrows(
                RecordValidationException.class,
                () -> service.createRecord(
                        businessObjectId,
                        Map.of("discount_rate", -5),
                        UUID.randomUUID()
                )
        );
        assertEquals("VALUE_BELOW_MINIMUM", createEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(recordId);
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(new LinkedHashMap<>(Map.of("discount_rate", 15)));

        when(recordRepository.findById(recordId))
                .thenReturn(Optional.of(existingRecord));

        RecordValidationException updateEx = assertThrows(
                RecordValidationException.class,
                () -> service.updateRecord(
                        businessObjectId,
                        recordId,
                        Map.of("discount_rate", 105.5)
                )
        );
        assertEquals("VALUE_ABOVE_MAXIMUM", updateEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());
        assertEquals(15, existingRecord.getRecordData().get("discount_rate"));
    }

    @Test
    void patternValidationShouldAcceptMatchingAndRejectNonMatchingValues() {
        BusinessObjectField skuField = fieldWithPattern(
                "sku_code",
                "TEXT",
                true,
                "^[A-Z]{3}-\\d{4}$"
        );

        ValidationResult validResult = validationEngine.validate(
                context(List.of(skuField), Map.of("sku_code", "ERP-1042"))
        );
        assertTrue(validResult.valid());
        assertTrue(validResult.errors().isEmpty());

        ValidationResult invalidResult = validationEngine.validate(
                context(List.of(skuField), Map.of("sku_code", "erp_1042"))
        );
        assertFalse(invalidResult.valid());
        assertEquals(1, invalidResult.errors().size());
        assertEquals("sku_code", invalidResult.errors().get(0).fieldKey());
        assertEquals("PATTERN_MISMATCH", invalidResult.errors().get(0).code());
    }

    @Test
    void patternValidationShouldIgnoreMissingBlankOrUnrelatedFieldPatterns() {
        BusinessObjectField unpatternedText = field("notes", "LONG_TEXT", false);
        BusinessObjectField blankPatternText = fieldWithPattern("code", "TEXT", false, "   ");
        BusinessObjectField accidentalPattern = fieldWithPattern(
                "employee_count",
                "NUMBER",
                false,
                "^[A-Z]+$"
        );

        ValidationResult result = validationEngine.validate(
                context(
                        List.of(unpatternedText, blankPatternText, accidentalPattern),
                        Map.of(
                                "notes", "Any free-form text 12345!",
                                "code", "any-value",
                                "employee_count", 42
                        )
                )
        );

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void emailValidationShouldAcceptValidAndRejectInvalidEmailsAndEnforceCustomPattern() {
        BusinessObjectField emailField = field("contact_email", "EMAIL", true);

        assertTrue(
                validationEngine.validate(
                        context(List.of(emailField), Map.of("contact_email", "alice.smith@maxerp.com"))
                ).valid()
        );

        ValidationResult invalidEmailResult = validationEngine.validate(
                context(List.of(emailField), Map.of("contact_email", "not-an-email"))
        );
        assertFalse(invalidEmailResult.valid());
        assertEquals(1, invalidEmailResult.errors().size());
        assertEquals("contact_email", invalidEmailResult.errors().get(0).fieldKey());
        assertEquals("INVALID_EMAIL", invalidEmailResult.errors().get(0).code());

        BusinessObjectField corporateEmailField = fieldWithPattern(
                "corporate_email",
                "EMAIL",
                true,
                "^[A-Za-z0-9._%+-]+@maxerp\\.com$"
        );

        assertTrue(
                validationEngine.validate(
                        context(List.of(corporateEmailField), Map.of("corporate_email", "bob@maxerp.com"))
                ).valid()
        );

        ValidationResult wrongDomainResult = validationEngine.validate(
                context(List.of(corporateEmailField), Map.of("corporate_email", "bob@example.org"))
        );
        assertFalse(wrongDomainResult.valid());
        assertEquals(1, wrongDomainResult.errors().size());
        assertEquals("corporate_email", wrongDomainResult.errors().get(0).fieldKey());
        assertEquals("PATTERN_MISMATCH", wrongDomainResult.errors().get(0).code());
    }

    @Test
    void phoneValidationShouldAcceptValidAndRejectInvalidPhones() {
        BusinessObjectField phoneField = field("contact_phone", "PHONE", true);

        assertTrue(
                validationEngine.validate(
                        context(List.of(phoneField), Map.of("contact_phone", "+1 (555) 234-5678"))
                ).valid()
        );
        assertTrue(
                validationEngine.validate(
                        context(List.of(phoneField), Map.of("contact_phone", "9876543210"))
                ).valid()
        );

        ValidationResult tooShortResult = validationEngine.validate(
                context(List.of(phoneField), Map.of("contact_phone", "12345"))
        );
        assertFalse(tooShortResult.valid());
        assertEquals(1, tooShortResult.errors().size());
        assertEquals("contact_phone", tooShortResult.errors().get(0).fieldKey());
        assertEquals("INVALID_PHONE", tooShortResult.errors().get(0).code());

        ValidationResult alphaPhoneResult = validationEngine.validate(
                context(List.of(phoneField), Map.of("contact_phone", "+1-800-MAX-ERP"))
        );
        assertFalse(alphaPhoneResult.valid());
        assertEquals(1, alphaPhoneResult.errors().size());
        assertEquals("contact_phone", alphaPhoneResult.errors().get(0).fieldKey());
        assertEquals("INVALID_PHONE", alphaPhoneResult.errors().get(0).code());
    }

    @Test
    void dateAndDateTimeValidationShouldAcceptValidIsoAndTemporalValuesAndRejectInvalidFormats() {
        BusinessObjectField dateField = field("start_date", "DATE", false);
        BusinessObjectField dateTimeField = field("scheduled_at", "DATETIME", false);

        assertTrue(
                validationEngine.validate(
                        context(
                                List.of(dateField, dateTimeField),
                                Map.of(
                                        "start_date", "2026-10-13",
                                        "scheduled_at", "2026-10-13T14:30:00+05:30"
                                )
                        )
                ).valid()
        );

        assertTrue(
                validationEngine.validate(
                        context(
                                List.of(dateField, dateTimeField),
                                Map.of(
                                        "start_date", LocalDate.of(2026, 10, 13),
                                        "scheduled_at", "2026-10-13T14:30"
                                )
                        )
                ).valid()
        );

        assertTrue(
                validationEngine.validate(
                        context(
                                List.of(dateTimeField),
                                Map.of("scheduled_at", LocalDateTime.of(2026, 10, 13, 14, 30))
                        )
                ).valid()
        );

        assertTrue(
                validationEngine.validate(
                        context(
                                List.of(dateTimeField),
                                Map.of("scheduled_at", OffsetDateTime.parse("2026-10-13T14:30:00Z"))
                        )
                ).valid()
        );

        assertTrue(
                validationEngine.validate(
                        context(
                                List.of(dateTimeField),
                                Map.of("scheduled_at", Instant.parse("2026-10-13T14:30:00Z"))
                        )
                ).valid()
        );

        ValidationResult invalidDateAndDateTime = validationEngine.validate(
                context(
                        List.of(dateField, dateTimeField),
                        Map.of(
                                "start_date", "13/10/2026",
                                "scheduled_at", "2026-10-13"
                        )
                )
        );
        assertFalse(invalidDateAndDateTime.valid());
        assertEquals(2, invalidDateAndDateTime.errors().size());
        assertEquals("start_date", invalidDateAndDateTime.errors().get(0).fieldKey());
        assertEquals("INVALID_DATE", invalidDateAndDateTime.errors().get(0).code());
        assertEquals("scheduled_at", invalidDateAndDateTime.errors().get(1).fieldKey());
        assertEquals("INVALID_DATETIME", invalidDateAndDateTime.errors().get(1).code());
    }

    @Test
    void patternAndFormatValidationShouldAllowOptionalEmptyValuesAndPreserveRequiredBehavior() {
        List<BusinessObjectField> optionalFields = List.of(
                fieldWithPattern("sku_code", "TEXT", false, "^[A-Z]{3}-\\d{4}$"),
                field("contact_email", "EMAIL", false),
                field("contact_phone", "PHONE", false),
                field("start_date", "DATE", false),
                field("scheduled_at", "DATETIME", false)
        );

        assertTrue(
                validationEngine.validate(context(optionalFields, Map.of())).valid()
        );

        Map<String, Object> explicitNullsAndBlanks = new LinkedHashMap<>();
        explicitNullsAndBlanks.put("sku_code", null);
        explicitNullsAndBlanks.put("contact_email", "");
        explicitNullsAndBlanks.put("contact_phone", "   ");
        explicitNullsAndBlanks.put("start_date", null);
        explicitNullsAndBlanks.put("scheduled_at", "");

        assertTrue(
                validationEngine.validate(context(optionalFields, explicitNullsAndBlanks)).valid()
        );

        BusinessObjectField requiredEmailField = field("contact_email", "EMAIL", true);
        ValidationResult requiredBlankResult = validationEngine.validate(
                context(List.of(requiredEmailField), Map.of("contact_email", "   "))
        );
        assertFalse(requiredBlankResult.valid());
        assertEquals(1, requiredBlankResult.errors().size());
        assertEquals("REQUIRED", requiredBlankResult.errors().get(0).code());
    }

    @Test
    void patternValidationShouldReturnControlledErrorWhenConfiguredPatternIsMalformed() {
        BusinessObjectField malformedPatternField = fieldWithPattern(
                "custom_code",
                "TEXT",
                true,
                "[unclosed-group"
        );

        ValidationResult result = validationEngine.validate(
                context(List.of(malformedPatternField), Map.of("custom_code", "ABC-123"))
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("custom_code", result.errors().get(0).fieldKey());
        assertEquals("INVALID_VALIDATION_PATTERN", result.errors().get(0).code());
    }

    @Test
    void createAndUpdateRecordShouldRejectInvalidFormatsAndPatternsWithoutPersistence() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        BusinessObject businessObject = new BusinessObject();

        BusinessObjectField codeField = fieldWithPattern(
                "vendor_code",
                "TEXT",
                true,
                "^VND-\\d{4}$"
        );
        BusinessObjectField emailField = field("vendor_email", "EMAIL", true);

        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(codeField, emailField));

        RecordValidationException createEx = assertThrows(
                RecordValidationException.class,
                () -> service.createRecord(
                        businessObjectId,
                        Map.of(
                                "vendor_code", "INVALID",
                                "vendor_email", "contact@vendor.com"
                        ),
                        UUID.randomUUID()
                )
        );
        assertEquals("PATTERN_MISMATCH", createEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(recordId);
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(new LinkedHashMap<>(Map.of(
                "vendor_code", "VND-1001",
                "vendor_email", "contact@vendor.com"
        )));

        when(recordRepository.findById(recordId))
                .thenReturn(Optional.of(existingRecord));

        RecordValidationException updateEx = assertThrows(
                RecordValidationException.class,
                () -> service.updateRecord(
                        businessObjectId,
                        recordId,
                        Map.of("vendor_email", "invalid-email")
                )
        );
        assertEquals("INVALID_EMAIL", updateEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());
        assertEquals("VND-1001", existingRecord.getRecordData().get("vendor_code"));
        assertEquals("contact@vendor.com", existingRecord.getRecordData().get("vendor_email"));
    }

    @Test
    void singleReferenceShouldAcceptExistingRecordAndRejectNonexistentTarget() {
        UUID fieldId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();
        UUID existingTargetRecordId = UUID.randomUUID();
        UUID nonexistentTargetRecordId = UUID.randomUUID();

        BusinessObject targetObject = new BusinessObject();
        targetObject.setId(targetObjectId);

        BusinessObjectField accountRefField = field("account_ref", "REFERENCE", true);
        accountRefField.setId(fieldId);

        BusinessObjectFieldReference referenceConfig =
                referenceConfig(accountRefField, targetObject, false);

        when(referenceRepository.findByFieldId(fieldId))
                .thenReturn(Optional.of(referenceConfig));

        BusinessObjectRecord existingTargetRecord =
                referenceRecord(existingTargetRecordId, targetObjectId);

        when(referenceRecordRepository.findAllById(any()))
                .thenAnswer(invocation -> {
                    Iterable<UUID> ids = invocation.getArgument(0);
                    List<BusinessObjectRecord> result = new ArrayList<>();
                    for (UUID id : ids) {
                        if (existingTargetRecordId.equals(id)) {
                            result.add(existingTargetRecord);
                        }
                    }
                    return result;
                });

        ValidationResult validResult = validationEngine.validate(
                context(
                        List.of(accountRefField),
                        Map.of("account_ref", existingTargetRecordId.toString())
                )
        );
        assertTrue(validResult.valid());
        assertTrue(validResult.errors().isEmpty());

        ValidationResult nonexistentResult = validationEngine.validate(
                context(
                        List.of(accountRefField),
                        Map.of("account_ref", nonexistentTargetRecordId.toString())
                )
        );
        assertFalse(nonexistentResult.valid());
        assertEquals(1, nonexistentResult.errors().size());
        assertEquals("account_ref", nonexistentResult.errors().get(0).fieldKey());
        assertEquals("INVALID_REFERENCE", nonexistentResult.errors().get(0).code());
    }

    @Test
    void singleReferenceShouldRejectRecordBelongingToDifferentBusinessObject() {
        UUID fieldId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();
        UUID wrongObjectId = UUID.randomUUID();
        UUID wrongObjectRecordId = UUID.randomUUID();

        BusinessObject targetObject = new BusinessObject();
        targetObject.setId(targetObjectId);

        BusinessObjectField accountRefField = field("account_ref", "REFERENCE", true);
        accountRefField.setId(fieldId);

        BusinessObjectFieldReference referenceConfig =
                referenceConfig(accountRefField, targetObject, false);

        when(referenceRepository.findByFieldId(fieldId))
                .thenReturn(Optional.of(referenceConfig));

        BusinessObjectRecord wrongObjectRecord =
                referenceRecord(wrongObjectRecordId, wrongObjectId);

        when(referenceRecordRepository.findAllById(any()))
                .thenReturn(List.of(wrongObjectRecord));

        ValidationResult result = validationEngine.validate(
                context(
                        List.of(accountRefField),
                        Map.of("account_ref", wrongObjectRecordId)
                )
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("account_ref", result.errors().get(0).fieldKey());
        assertEquals("INVALID_REFERENCE", result.errors().get(0).code());
    }

    @Test
    void referenceValidationShouldAllowOptionalEmptyValuesAndRejectMissingReferenceConfiguration() {
        UUID fieldId = UUID.randomUUID();
        BusinessObjectField unconfiguredRef = field("department_ref", "REFERENCE", false);
        unconfiguredRef.setId(fieldId);

        when(referenceRepository.findByFieldId(fieldId))
                .thenReturn(Optional.empty());

        assertTrue(
                validationEngine.validate(
                        context(List.of(unconfiguredRef), Map.of())
                ).valid()
        );

        assertTrue(
                validationEngine.validate(
                        context(List.of(unconfiguredRef), Map.of("department_ref", "   "))
                ).valid()
        );

        ValidationResult unconfiguredWithValue = validationEngine.validate(
                context(
                        List.of(unconfiguredRef),
                        Map.of("department_ref", UUID.randomUUID())
                )
        );
        assertFalse(unconfiguredWithValue.valid());
        assertEquals(1, unconfiguredWithValue.errors().size());
        assertEquals("department_ref", unconfiguredWithValue.errors().get(0).fieldKey());
        assertEquals("INVALID_REFERENCE", unconfiguredWithValue.errors().get(0).code());
    }

    @Test
    void multipleReferencesShouldValidateBatchAndRespectAllowMultipleSetting() {
        UUID fieldId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();
        UUID record1Id = UUID.randomUUID();
        UUID record2Id = UUID.randomUUID();

        BusinessObject targetObject = new BusinessObject();
        targetObject.setId(targetObjectId);

        BusinessObjectField tagsField = field("tags_ref", "REFERENCE", true);
        tagsField.setId(fieldId);

        BusinessObjectFieldReference allowMultiConfig =
                referenceConfig(tagsField, targetObject, true);

        when(referenceRepository.findByFieldId(fieldId))
                .thenReturn(Optional.of(allowMultiConfig));

        BusinessObjectRecord r1 = referenceRecord(record1Id, targetObjectId);
        BusinessObjectRecord r2 = referenceRecord(record2Id, targetObjectId);

        when(referenceRecordRepository.findAllById(any()))
                .thenReturn(List.of(r1, r2));

        ValidationResult validMulti = validationEngine.validate(
                context(
                        List.of(tagsField),
                        Map.of("tags_ref", List.of(record1Id.toString(), record2Id.toString()))
                )
        );
        assertTrue(validMulti.valid());
        assertTrue(validMulti.errors().isEmpty());

        BusinessObjectFieldReference disallowMultiConfig =
                referenceConfig(tagsField, targetObject, false);

        when(referenceRepository.findByFieldId(fieldId))
                .thenReturn(Optional.of(disallowMultiConfig));

        ValidationResult rejectedWhenMultiDisallowed = validationEngine.validate(
                context(
                        List.of(tagsField),
                        Map.of("tags_ref", List.of(record1Id.toString(), record2Id.toString()))
                )
        );
        assertFalse(rejectedWhenMultiDisallowed.valid());
        assertEquals(1, rejectedWhenMultiDisallowed.errors().size());
        assertEquals("tags_ref", rejectedWhenMultiDisallowed.errors().get(0).fieldKey());
        assertEquals("INVALID_REFERENCE", rejectedWhenMultiDisallowed.errors().get(0).code());
    }

    @Test
    void userValidationShouldAcceptExistingActiveUserAndRejectUnknownUser() {
        UUID activeUserId = UUID.randomUUID();
        UUID unknownUserId = UUID.randomUUID();

        BusinessObjectField userField = field("assigned_to", "USER", true);

        User activeUser = user(activeUserId, "ACTIVE");

        when(userRepository.findAllById(any()))
                .thenAnswer(invocation -> {
                    Iterable<UUID> ids = invocation.getArgument(0);
                    List<User> result = new ArrayList<>();
                    for (UUID id : ids) {
                        if (activeUserId.equals(id)) {
                            result.add(activeUser);
                        }
                    }
                    return result;
                });

        ValidationResult validUserResult = validationEngine.validate(
                context(List.of(userField), Map.of("assigned_to", activeUserId.toString()))
        );
        assertTrue(validUserResult.valid());
        assertTrue(validUserResult.errors().isEmpty());

        ValidationResult unknownUserResult = validationEngine.validate(
                context(List.of(userField), Map.of("assigned_to", unknownUserId.toString()))
        );
        assertFalse(unknownUserResult.valid());
        assertEquals(1, unknownUserResult.errors().size());
        assertEquals("assigned_to", unknownUserResult.errors().get(0).fieldKey());
        assertEquals("INVALID_USER", unknownUserResult.errors().get(0).code());
    }

    @Test
    void userValidationShouldRejectInactiveUserAndMalformedIdAndAllowOptionalEmpty() {
        UUID inactiveUserId = UUID.randomUUID();
        BusinessObjectField userField = field("assigned_to", "USER", false);

        User inactiveUser = user(inactiveUserId, "INACTIVE");

        when(userRepository.findAllById(any()))
                .thenReturn(List.of(inactiveUser));

        ValidationResult inactiveResult = validationEngine.validate(
                context(List.of(userField), Map.of("assigned_to", inactiveUserId))
        );
        assertFalse(inactiveResult.valid());
        assertEquals(1, inactiveResult.errors().size());
        assertEquals("assigned_to", inactiveResult.errors().get(0).fieldKey());
        assertEquals("INVALID_USER", inactiveResult.errors().get(0).code());

        ValidationResult malformedIdResult = validationEngine.validate(
                context(List.of(userField), Map.of("assigned_to", "not-a-valid-uuid"))
        );
        assertFalse(malformedIdResult.valid());
        assertEquals(1, malformedIdResult.errors().size());
        assertEquals("assigned_to", malformedIdResult.errors().get(0).fieldKey());
        assertEquals("INVALID_USER", malformedIdResult.errors().get(0).code());

        assertTrue(
                validationEngine.validate(
                        context(List.of(userField), Map.of())
                ).valid()
        );
        assertTrue(
                validationEngine.validate(
                        context(List.of(userField), Map.of("assigned_to", "   "))
                ).valid()
        );
    }

    @Test
    void createAndUpdateRecordShouldRejectInvalidReferenceAndUserValuesWithoutPersistence() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        UUID refFieldId = UUID.randomUUID();

        BusinessObject businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);

        BusinessObject targetObject = new BusinessObject();
        targetObject.setId(targetObjectId);

        BusinessObjectField refField = field("supplier_ref", "REFERENCE", true);
        refField.setId(refFieldId);

        BusinessObjectField userField = field("owner_user", "USER", true);

        BusinessObjectFieldReference refConfig =
                referenceConfig(refField, targetObject, false);

        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(refField, userField));
        when(referenceRepository.findByFieldId(refFieldId))
                .thenReturn(Optional.of(refConfig));

        UUID invalidRefId = UUID.randomUUID();
        UUID invalidUserId = UUID.randomUUID();

        when(referenceRecordRepository.findAllById(any()))
                .thenReturn(List.of());
        when(userRepository.findAllById(any()))
                .thenReturn(List.of());

        RecordValidationException createEx = assertThrows(
                RecordValidationException.class,
                () -> service.createRecord(
                        businessObjectId,
                        Map.of(
                                "supplier_ref", invalidRefId,
                                "owner_user", invalidUserId
                        ),
                        UUID.randomUUID()
                )
        );
        assertEquals(2, createEx.getErrors().size());
        verify(recordRepository, never()).save(any());

        UUID validSupplierRefId = UUID.randomUUID();
        BusinessObjectRecord validSupplierRecord =
                referenceRecord(validSupplierRefId, targetObjectId);

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(recordId);
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(new LinkedHashMap<>(Map.of(
                "supplier_ref", validSupplierRefId.toString(),
                "owner_user", UUID.randomUUID().toString()
        )));

        when(recordRepository.findById(recordId))
                .thenReturn(Optional.of(existingRecord));
        when(referenceRecordRepository.findAllById(any()))
                .thenReturn(List.of(validSupplierRecord));

        RecordValidationException updateEx = assertThrows(
                RecordValidationException.class,
                () -> service.updateRecord(
                        businessObjectId,
                        recordId,
                        Map.of("owner_user", "not-a-uuid")
                )
        );
        assertEquals("INVALID_USER", updateEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());
        assertEquals(validSupplierRefId.toString(), existingRecord.getRecordData().get("supplier_ref"));
    }

    @Test
    void uniqueFieldShouldAcceptDistinctValueOnCreate() {
        UUID businessObjectId = UUID.randomUUID();
        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField codeField = fieldWithUnique("tax_id", "TEXT", true, true);

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(UUID.randomUUID());
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(Map.of("tax_id", "TAX-1111"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(existingRecord));

        ValidationResult result = validationEngine.validate(
                new ValidationContext(
                        bo,
                        List.of(codeField),
                        Map.of("tax_id", "TAX-2222"),
                        ValidationOperation.CREATE,
                        null
                )
        );

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void uniqueFieldShouldRejectDuplicateValueOnCreateWithoutLeakingRecordContents() {
        UUID businessObjectId = UUID.randomUUID();
        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField codeField = fieldWithUnique("tax_id", "TEXT", true, true);

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(UUID.randomUUID());
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(Map.of("tax_id", "TAX-1111", "sensitive_notes", "secret"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(existingRecord));

        ValidationResult result = validationEngine.validate(
                new ValidationContext(
                        bo,
                        List.of(codeField),
                        Map.of("tax_id", "TAX-1111"),
                        ValidationOperation.CREATE,
                        null
                )
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("tax_id", result.errors().get(0).fieldKey());
        assertEquals("DUPLICATE_VALUE", result.errors().get(0).code());
        assertEquals("Value for field tax_id must be unique", result.errors().get(0).message());
        assertFalse(result.errors().get(0).message().contains("secret"));
        assertFalse(result.errors().get(0).message().contains(existingRecord.getId().toString()));
    }

    @Test
    void uniqueFieldShouldRejectDuplicateValueFromAnotherRecordOnUpdate() {
        UUID businessObjectId = UUID.randomUUID();
        UUID currentRecordId = UUID.randomUUID();
        UUID otherRecordId = UUID.randomUUID();

        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField codeField = fieldWithUnique("sku_code", "TEXT", true, true);

        BusinessObjectRecord otherRecord = new BusinessObjectRecord();
        otherRecord.setId(otherRecordId);
        otherRecord.setBusinessObjectId(businessObjectId);
        otherRecord.setRecordData(Map.of("sku_code", "SKU-999"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(otherRecord));

        ValidationResult result = validationEngine.validate(
                new ValidationContext(
                        bo,
                        List.of(codeField),
                        Map.of("sku_code", "SKU-999"),
                        ValidationOperation.UPDATE,
                        currentRecordId
                )
        );

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
        assertEquals("sku_code", result.errors().get(0).fieldKey());
        assertEquals("DUPLICATE_VALUE", result.errors().get(0).code());
    }

    @Test
    void uniqueFieldShouldAllowUnchangedValueForSameRecordOnUpdate() {
        UUID businessObjectId = UUID.randomUUID();
        UUID currentRecordId = UUID.randomUUID();

        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField codeField = fieldWithUnique("sku_code", "TEXT", true, true);

        BusinessObjectRecord currentRecord = new BusinessObjectRecord();
        currentRecord.setId(currentRecordId);
        currentRecord.setBusinessObjectId(businessObjectId);
        currentRecord.setRecordData(Map.of("sku_code", "SKU-555"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(currentRecord));

        ValidationResult result = validationEngine.validate(
                new ValidationContext(
                        bo,
                        List.of(codeField),
                        Map.of("sku_code", "SKU-555"),
                        ValidationOperation.UPDATE,
                        currentRecordId
                )
        );

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void uniqueFieldShouldIgnoreMissingNullAndBlankValues() {
        UUID businessObjectId = UUID.randomUUID();
        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField optionalUnique = fieldWithUnique("passport_no", "TEXT", false, true);

        BusinessObjectRecord existingWithNull = new BusinessObjectRecord();
        existingWithNull.setId(UUID.randomUUID());
        existingWithNull.setBusinessObjectId(businessObjectId);
        existingWithNull.setRecordData(new LinkedHashMap<>());

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(existingWithNull));

        assertTrue(
                validationEngine.validate(
                        new ValidationContext(
                                bo,
                                List.of(optionalUnique),
                                Map.of(),
                                ValidationOperation.CREATE,
                                null
                        )
                ).valid()
        );

        Map<String, Object> nullValueMap = new LinkedHashMap<>();
        nullValueMap.put("passport_no", null);
        assertTrue(
                validationEngine.validate(
                        new ValidationContext(
                                bo,
                                List.of(optionalUnique),
                                nullValueMap,
                                ValidationOperation.CREATE,
                                null
                        )
                ).valid()
        );

        assertTrue(
                validationEngine.validate(
                        new ValidationContext(
                                bo,
                                List.of(optionalUnique),
                                Map.of("passport_no", "   "),
                                ValidationOperation.CREATE,
                                null
                        )
                ).valid()
        );
    }

    @Test
    void uniqueFieldShouldScopeUniquenessByFieldKeyAndBusinessObject() {
        UUID bo1Id = UUID.randomUUID();
        UUID bo2Id = UUID.randomUUID();

        BusinessObject bo1 = new BusinessObject();
        bo1.setId(bo1Id);

        BusinessObjectField bo1Field = fieldWithUnique("code", "TEXT", true, true);
        BusinessObjectField unconstrainedField = fieldWithUnique("code", "TEXT", true, false);

        BusinessObjectRecord bo2RecordWithSameCode = new BusinessObjectRecord();
        bo2RecordWithSameCode.setId(UUID.randomUUID());
        bo2RecordWithSameCode.setBusinessObjectId(bo2Id);
        bo2RecordWithSameCode.setRecordData(Map.of("code", "COMMON-CODE"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(bo1Id))
                .thenReturn(List.of());

        ValidationResult resultBo1 = validationEngine.validate(
                new ValidationContext(
                        bo1,
                        List.of(bo1Field),
                        Map.of("code", "COMMON-CODE"),
                        ValidationOperation.CREATE,
                        null
                )
        );
        assertTrue(resultBo1.valid());

        BusinessObjectRecord bo1RecordWithSameCode = new BusinessObjectRecord();
        bo1RecordWithSameCode.setId(UUID.randomUUID());
        bo1RecordWithSameCode.setBusinessObjectId(bo1Id);
        bo1RecordWithSameCode.setRecordData(Map.of("code", "COMMON-CODE"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(bo1Id))
                .thenReturn(List.of(bo1RecordWithSameCode));

        ValidationResult unconstrainedResult = validationEngine.validate(
                new ValidationContext(
                        bo1,
                        List.of(unconstrainedField),
                        Map.of("code", "COMMON-CODE"),
                        ValidationOperation.CREATE,
                        null
                )
        );
        assertTrue(unconstrainedResult.valid());
    }

    @Test
    void uniqueFieldShouldCompareNumericAndStringValuesConsistentlyWithJsonbStorage() {
        UUID businessObjectId = UUID.randomUUID();
        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField numericUnique = fieldWithUnique("badge_number", "NUMBER", true, true);
        BusinessObjectField textUnique = fieldWithUnique("slug", "TEXT", true, true);

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(UUID.randomUUID());
        existingRecord.setBusinessObjectId(businessObjectId);
        existingRecord.setRecordData(Map.of(
                "badge_number", 100,
                "slug", "max-enterprise"
        ));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(existingRecord));

        ValidationResult numericDuplicate = validationEngine.validate(
                new ValidationContext(
                        bo,
                        List.of(numericUnique),
                        Map.of("badge_number", 100.0),
                        ValidationOperation.CREATE,
                        null
                )
        );
        assertFalse(numericDuplicate.valid());
        assertEquals("DUPLICATE_VALUE", numericDuplicate.errors().get(0).code());

        ValidationResult stringTrimmedDuplicate = validationEngine.validate(
                new ValidationContext(
                        bo,
                        List.of(textUnique),
                        Map.of("slug", "  max-enterprise  "),
                        ValidationOperation.CREATE,
                        null
                )
        );
        assertFalse(stringTrimmedDuplicate.valid());
        assertEquals("DUPLICATE_VALUE", stringTrimmedDuplicate.errors().get(0).code());
    }

    @Test
    void createAndUpdateRecordShouldRejectDuplicateUniqueValuesWithoutPersistence() {
        BusinessObjectRecordRepository recordRepository =
                mock(BusinessObjectRecordRepository.class);
        BusinessObjectRepository businessObjectRepository =
                mock(BusinessObjectRepository.class);
        BusinessObjectFieldRepository fieldRepository =
                mock(BusinessObjectFieldRepository.class);

        RecordValidationEngine engineWithUnique = new RecordValidationEngineImpl(
                List.of(
                        requiredFieldValidator,
                        dataTypeValidator,
                        choiceFieldValidator,
                        numericRangeValidator,
                        patternAndFormatValidator,
                        referenceFieldValidator,
                        userFieldValidator,
                        new UniqueValueValidator(recordRepository)
                )
        );

        BusinessObjectRecordService service = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                engineWithUnique
        );

        UUID businessObjectId = UUID.randomUUID();
        UUID record1Id = UUID.randomUUID();
        UUID record2Id = UUID.randomUUID();

        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);

        BusinessObjectField uniqueField = fieldWithUnique("license_key", "TEXT", true, true);

        BusinessObjectRecord existingRecord1 = new BusinessObjectRecord();
        existingRecord1.setId(record1Id);
        existingRecord1.setBusinessObjectId(businessObjectId);
        existingRecord1.setRecordData(new LinkedHashMap<>(Map.of("license_key", "LIC-AAAA")));

        when(businessObjectRepository.findById(businessObjectId))
                .thenReturn(Optional.of(bo));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(uniqueField));
        when(recordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(existingRecord1));

        RecordValidationException createEx = assertThrows(
                RecordValidationException.class,
                () -> service.createRecord(
                        businessObjectId,
                        Map.of("license_key", "LIC-AAAA"),
                        UUID.randomUUID()
                )
        );
        assertEquals("DUPLICATE_VALUE", createEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());

        BusinessObjectRecord existingRecord2 = new BusinessObjectRecord();
        existingRecord2.setId(record2Id);
        existingRecord2.setBusinessObjectId(businessObjectId);
        existingRecord2.setRecordData(new LinkedHashMap<>(Map.of("license_key", "LIC-BBBB")));

        when(recordRepository.findById(record2Id))
                .thenReturn(Optional.of(existingRecord2));
        when(recordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(existingRecord1, existingRecord2));

        RecordValidationException updateEx = assertThrows(
                RecordValidationException.class,
                () -> service.updateRecord(
                        businessObjectId,
                        record2Id,
                        Map.of("license_key", "LIC-AAAA")
                )
        );
        assertEquals("DUPLICATE_VALUE", updateEx.getErrors().get(0).code());
        verify(recordRepository, never()).save(any());
        assertEquals("LIC-BBBB", existingRecord2.getRecordData().get("license_key"));
    }

    @Test
    void deterministicValidatorOrderingRegardlessOfConstructorInputOrder() {
        RequiredFieldValidator reqValidator = new RequiredFieldValidator();
        DataTypeValidator dataValidator = new DataTypeValidator();
        ChoiceFieldValidator choiceValidator = new ChoiceFieldValidator(optionRepository);
        NumericRangeValidator rangeValidator = new NumericRangeValidator();
        PatternAndFormatValidator patternValidator = new PatternAndFormatValidator();
        ReferenceFieldValidator refValidator = new ReferenceFieldValidator(referenceRepository, referenceRecordRepository);
        UserFieldValidator userValidator = new UserFieldValidator(userRepository);
        UniqueValueValidator uniqueValidator = new UniqueValueValidator(referenceRecordRepository);

        RecordValidationEngineImpl reverseEngine = new RecordValidationEngineImpl(
                List.of(
                        uniqueValidator,
                        userValidator,
                        refValidator,
                        patternValidator,
                        rangeValidator,
                        choiceValidator,
                        dataValidator,
                        reqValidator
                )
        );

        List<RecordValidator> sortedValidators = reverseEngine.getValidators();
        assertEquals(8, sortedValidators.size());
        assertEquals(RequiredFieldValidator.class, sortedValidators.get(0).getClass());
        assertEquals(DataTypeValidator.class, sortedValidators.get(1).getClass());
        assertEquals(ChoiceFieldValidator.class, sortedValidators.get(2).getClass());
        assertEquals(NumericRangeValidator.class, sortedValidators.get(3).getClass());
        assertEquals(PatternAndFormatValidator.class, sortedValidators.get(4).getClass());
        assertEquals(ReferenceFieldValidator.class, sortedValidators.get(5).getClass());
        assertEquals(UserFieldValidator.class, sortedValidators.get(6).getClass());
        assertEquals(UniqueValueValidator.class, sortedValidators.get(7).getClass());

        UUID boId = UUID.randomUUID();
        BusinessObject bo = new BusinessObject();
        bo.setId(boId);

        BusinessObjectField nameField = field("name", "TEXT", true);
        BusinessObjectField codeField = fieldWithUnique("code", "TEXT", false, true);

        BusinessObjectRecord existingRecord = new BusinessObjectRecord();
        existingRecord.setId(UUID.randomUUID());
        existingRecord.setBusinessObjectId(boId);
        existingRecord.setRecordData(Map.of("code", "EXISTING_CODE"));

        when(referenceRecordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(boId))
                .thenReturn(List.of(existingRecord));

        ValidationContext context = new ValidationContext(
                bo,
                List.of(nameField, codeField),
                Map.of("code", "EXISTING_CODE"),
                ValidationOperation.CREATE,
                null
        );

        ValidationResult result = reverseEngine.validate(context);
        assertEquals(false, result.valid());
        assertEquals(2, result.errors().size());
        assertEquals("REQUIRED", result.errors().get(0).code());
        assertEquals("name", result.errors().get(0).fieldKey());
        assertEquals("DUPLICATE_VALUE", result.errors().get(1).code());
        assertEquals("code", result.errors().get(1).fieldKey());
    }

    @Test
    void requiredBlankFieldErrorsShouldTakePrecedenceOverDownstreamFormatErrors() {
        BusinessObjectField emailField = fieldWithPattern("email", "EMAIL", true, null);
        BusinessObjectField phoneField = fieldWithPattern("phone", "PHONE", true, null);
        BusinessObjectField dateField = fieldWithPattern("date", "DATE", true, null);

        ValidationContext contextWithBlanks = context(
                List.of(emailField, phoneField, dateField),
                Map.of(
                        "email", "   ",
                        "phone", "",
                        "date", "   "
                )
        );

        ValidationResult blankResult = validationEngine.validate(contextWithBlanks);
        assertEquals(false, blankResult.valid());
        assertEquals(3, blankResult.errors().size());

        for (ValidationError error : blankResult.errors()) {
            assertEquals("REQUIRED", error.code());
            assertEquals("Field is required", error.message());
        }

        ValidationContext contextWithInvalidFormats = context(
                List.of(emailField, phoneField, dateField),
                Map.of(
                        "email", "not-an-email",
                        "phone", "123",
                        "date", "not-a-date"
                )
        );

        ValidationResult invalidResult = validationEngine.validate(contextWithInvalidFormats);
        assertEquals(false, invalidResult.valid());
        assertEquals(3, invalidResult.errors().size());
        assertEquals("INVALID_EMAIL", invalidResult.errors().get(0).code());
        assertEquals("INVALID_PHONE", invalidResult.errors().get(1).code());
        assertEquals("INVALID_DATE", invalidResult.errors().get(2).code());
    }

    @Test
    void multiReferenceShouldAcceptValidCollectionWithDuplicateIds() {
        UUID boId = UUID.randomUUID();
        BusinessObject targetBo = new BusinessObject();
        targetBo.setId(boId);

        BusinessObjectField refField = field("departments", "REFERENCE", false);
        refField.setId(UUID.randomUUID());

        BusinessObjectFieldReference config = referenceConfig(refField, targetBo, true);
        when(referenceRepository.findByFieldId(refField.getId()))
                .thenReturn(Optional.of(config));

        UUID dept1Id = UUID.randomUUID();
        BusinessObjectRecord dept1 = referenceRecord(dept1Id, boId);

        when(referenceRecordRepository.findAllById(any()))
                .thenReturn(List.of(dept1));

        ValidationContext contextWithDuplicates = context(
                List.of(refField),
                Map.of("departments", List.of(dept1Id, dept1Id, dept1Id.toString()))
        );

        ValidationResult result = validationEngine.validate(contextWithDuplicates);
        assertEquals(true, result.valid());
        assertEquals(0, result.errors().size());

        UUID unknownId = UUID.randomUUID();
        ValidationContext contextWithUnknown = context(
                List.of(refField),
                Map.of("departments", List.of(dept1Id, unknownId))
        );

        ValidationResult failResult = validationEngine.validate(contextWithUnknown);
        assertEquals(false, failResult.valid());
        assertEquals(1, failResult.errors().size());
        assertEquals("INVALID_REFERENCE", failResult.errors().get(0).code());
    }

    private BusinessObjectField fieldWithUnique(
            String fieldKey,
            String fieldType,
            boolean required,
            boolean uniqueValue
    ) {
        BusinessObjectField field = field(fieldKey, fieldType, required);
        field.setUniqueValue(uniqueValue);
        return field;
    }

    private BusinessObjectFieldReference referenceConfig(
            BusinessObjectField field,
            BusinessObject targetObject,
            boolean allowMultiple
    ) {
        BusinessObjectFieldReference ref = new BusinessObjectFieldReference();
        ref.setField(field);
        ref.setTargetBusinessObject(targetObject);
        ref.setAllowMultiple(allowMultiple);
        return ref;
    }

    private BusinessObjectRecord referenceRecord(UUID id, UUID businessObjectId) {
        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(id);
        record.setBusinessObjectId(businessObjectId);
        return record;
    }

    private User user(UUID id, String status) {
        User user = new User();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setStatus(status);
        return user;
    }

    private BusinessObjectField fieldWithPattern(
            String fieldKey,
            String fieldType,
            boolean required,
            String validationPattern
    ) {
        BusinessObjectField field = field(fieldKey, fieldType, required);
        field.setValidationPattern(validationPattern);
        return field;
    }

    private BusinessObjectField fieldWithRange(
            String fieldKey,
            String fieldType,
            boolean required,
            BigDecimal minValue,
            BigDecimal maxValue
    ) {
        BusinessObjectField field = field(fieldKey, fieldType, required);
        field.setMinValue(minValue);
        field.setMaxValue(maxValue);
        return field;
    }

    private BusinessObjectFieldOption option(
            String value,
            String label,
            boolean active
    ) {
        BusinessObjectFieldOption option = new BusinessObjectFieldOption();
        option.setOptionValue(value);
        option.setOptionLabel(label);
        option.setActive(active);
        return option;
    }

    private BusinessObjectField field(
            String fieldKey,
            String fieldType,
            boolean required
    ) {
        BusinessObjectField field = new BusinessObjectField();

        field.setFieldKey(fieldKey);
        field.setFieldType(fieldType);
        field.setRequired(required);

        return field;
    }

    private ValidationContext context(
            List<BusinessObjectField> fields,
            Map<String, Object> values
    ) {
        return new ValidationContext(
                null,
                fields,
                values,
                ValidationOperation.CREATE
        );
    }
}
