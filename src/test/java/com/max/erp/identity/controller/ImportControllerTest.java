package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.AuthorizationDecision;
import com.max.erp.identity.authorization.AuthorizationException;
import com.max.erp.identity.authorization.AuthorizationService;
import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.*;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.enums.ImportJobStatus;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    @Mock
    private ImportService importService;

    @Mock
    private ImportParserService importParserService;

    @Mock
    private ConfigurationImportJobRepository importJobRepository;

    @Mock
    private ImportMappingService importMappingService;

    @Mock
    private ImportValidationService importValidationService;

    @Mock
    private ImportExecutionService importExecutionService;

    @Mock
    private BusinessObjectRepository businessObjectRepository;

    @Mock
    private SecurityAuthorizationGuard authorizationGuard;

    @Mock
    private AuthorizationService authorizationService;

    private MockMvc mockMvc;

    private UUID businessObjectId;
    private UUID importId;
    private ConfigurationImportJob sampleJob;
    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        ImportController controller = new ImportController(
                importService,
                importParserService,
                importJobRepository,
                importMappingService,
                importValidationService,
                importExecutionService,
                businessObjectRepository,
                authorizationGuard,
                authorizationService
        );

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();

        businessObjectId = UUID.randomUUID();
        importId = UUID.randomUUID();

        sampleJob = new ConfigurationImportJob();
        sampleJob.setBusinessObjectId(businessObjectId);
        sampleJob.setStatus(ImportJobStatus.VALIDATED);
        sampleJob.setFileName("test.csv");
        sampleJob.setFileType("CSV");
        sampleJob.setTotalRows(10);
        sampleJob.setValidRows(10);
        sampleJob.setInvalidRows(0);
        sampleJob.setDuplicateRows(0);
        sampleJob.setProcessedRows(0);

        adminAuth = new UsernamePasswordAuthenticationToken("admin_user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ============================================================
    // EXECUTION ENDPOINT TESTS
    // ============================================================

    @Test
    void executeImportSuccess() throws Exception {
        when(importJobRepository.findById(importId)).thenReturn(Optional.of(sampleJob));

        UUID adminId = UUID.randomUUID();
        when(authorizationService.resolveUserId("admin_user")).thenReturn(adminId);

        ImportExecutionResponse executionResponse = new ImportExecutionResponse();
        executionResponse.setImportJobId(importId);
        executionResponse.setBusinessObjectId(businessObjectId);
        executionResponse.setStatus(ImportJobStatus.COMPLETED);
        executionResponse.setTotalRows(10);
        executionResponse.setProcessedRows(10);
        executionResponse.setFailedRows(0);
        executionResponse.setDuplicateRows(0);

        when(importExecutionService.execute(eq(businessObjectId), eq(importId), eq(adminId)))
                .thenReturn(executionResponse);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute",
                businessObjectId, importId)
                        .principal(adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.processedRows").value(10))
                .andExpect(jsonPath("$.failedRows").value(0))
                .andExpect(jsonPath("$.duplicateRows").value(0));

        verify(authorizationGuard).require(any(), isNull(), isNull(), eq("CONFIGURATION"), eq("MANAGE"));
        verify(importExecutionService).execute(businessObjectId, importId, adminId);
    }

    @Test
    void executeImportUnauthenticatedRejected() throws Exception {
        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute",
                businessObjectId, importId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));

        verify(importExecutionService, never()).execute(any(), any(), any());
    }

    @Test
    void executeImportUnauthorizedRejected() throws Exception {
        Authentication unauthorizedAuth = new UsernamePasswordAuthenticationToken("regular_user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        doThrow(new AuthorizationException(
                AuthorizationDecision.PERMISSION_DENIED.name(),
                "User lacks required permission"
        )).when(authorizationGuard).require(any(), isNull(), isNull(), eq("CONFIGURATION"), eq("MANAGE"));

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute",
                businessObjectId, importId)
                        .principal(unauthorizedAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));

        verify(importExecutionService, never()).execute(any(), any(), any());
    }

    @Test
    void executeImportMismatchRejected() throws Exception {
        UUID otherObjectId = UUID.randomUUID();
        sampleJob.setBusinessObjectId(otherObjectId);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(sampleJob));

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute",
                businessObjectId, importId)
                        .principal(adminAuth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(importExecutionService, never()).execute(any(), any(), any());
    }

    @Test
    void executeImportNotValidatedReturnsConflict() throws Exception {
        when(importJobRepository.findById(importId)).thenReturn(Optional.of(sampleJob));

        when(importExecutionService.execute(any(), any(), any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Import job is not in a VALIDATED state"
                ));

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute",
                businessObjectId, importId)
                        .principal(adminAuth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Import job is not in a VALIDATED state"));
    }

    @Test
    void executeImportConcurrentConflictPropagated() throws Exception {
        when(importJobRepository.findById(importId)).thenReturn(Optional.of(sampleJob));

        when(importExecutionService.execute(any(), any(), any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Import job is already executing or status was modified concurrently"
                ));

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute",
                businessObjectId, importId)
                        .principal(adminAuth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Import job is already executing or status was modified concurrently"));
    }

    // ============================================================
    // IMPORT HISTORY & DETAIL ENDPOINT TESTS
    // ============================================================

    @Test
    void getHistoryReturnsNewestFirst() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setId(businessObjectId);
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(bo));

        ConfigurationImportJob job1 = mock(ConfigurationImportJob.class);
        when(job1.getId()).thenReturn(UUID.randomUUID());
        when(job1.getBusinessObjectId()).thenReturn(businessObjectId);
        when(job1.getStatus()).thenReturn(ImportJobStatus.COMPLETED);
        when(job1.getFileName()).thenReturn("file1.csv");
        when(job1.getFileType()).thenReturn("CSV");
        when(job1.getCreatedAt()).thenReturn(Instant.now());

        ConfigurationImportJob job2 = mock(ConfigurationImportJob.class);
        when(job2.getId()).thenReturn(UUID.randomUUID());
        when(job2.getBusinessObjectId()).thenReturn(businessObjectId);
        when(job2.getStatus()).thenReturn(ImportJobStatus.COMPLETED_WITH_ERRORS);
        when(job2.getFileName()).thenReturn("file2.csv");
        when(job2.getFileType()).thenReturn("CSV");
        when(job2.getCreatedAt()).thenReturn(Instant.now().minusSeconds(3600));

        when(importJobRepository.findAllByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(job1, job2));

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/imports",
                businessObjectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fileName").value("file1.csv"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].fileName").value("file2.csv"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED_WITH_ERRORS"));
    }

    @Test
    void getJobDetailsMismatchReturnsNotFound() throws Exception {
        UUID otherObjectId = UUID.randomUUID();
        sampleJob.setBusinessObjectId(otherObjectId);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(sampleJob));

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}",
                businessObjectId, importId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getJobDetailsSuccess() throws Exception {
        sampleJob.setStatus(ImportJobStatus.COMPLETED);
        sampleJob.setProcessedRows(8);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(sampleJob));

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}",
                businessObjectId, importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fileName").value("test.csv"))
                .andExpect(jsonPath("$.processedRows").value(8));
    }

    // ============================================================
    // EXISTING ENDPOINTS REMAIN UNAFFECTED
    // ============================================================

    @Test
    void existingUploadEndpointRemainsUnaffected() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "col1,col2\n1,2".getBytes());

        ImportUploadResponse uploadResponse = new ImportUploadResponse();
        uploadResponse.setImportJobId(importId);
        uploadResponse.setStatus(ImportJobStatus.UPLOADED);

        when(importService.upload(eq(businessObjectId), any())).thenReturn(uploadResponse);

        mockMvc.perform(multipart("/api/v1/configuration/business-objects/{businessObjectId}/imports",
                businessObjectId).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }

    @Test
    void existingValidateEndpointRemainsUnaffected() throws Exception {
        ImportValidationResponse validationResponse = new ImportValidationResponse();
        validationResponse.setImportJobId(importId);
        validationResponse.setStatus(ImportJobStatus.VALIDATED);
        validationResponse.setTotalRows(5);
        validationResponse.setValidRows(5);

        when(importValidationService.validate(businessObjectId, importId)).thenReturn(validationResponse);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/validate",
                businessObjectId, importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDATED"))
                .andExpect(jsonPath("$.validRows").value(5));
    }

    @Test
    void existingMappingEndpointsRemainUnaffected() throws Exception {
        ImportMappingResponse mappingResponse = new ImportMappingResponse(
                importId, businessObjectId, List.of("ColA"), List.of(), List.of()
        );
        when(importMappingService.getMappingPreview(businessObjectId, importId)).thenReturn(mappingResponse);

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/mapping",
                businessObjectId, importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importJobId").value(importId.toString()));

        ImportMappingSaveResponse saveResponse = new ImportMappingSaveResponse(importId, businessObjectId, java.util.Map.of());
        when(importMappingService.saveMapping(eq(businessObjectId), eq(importId), any())).thenReturn(saveResponse);

        mockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/mapping",
                businessObjectId, importId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mapping\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importJobId").value(importId.toString()));
    }
}
