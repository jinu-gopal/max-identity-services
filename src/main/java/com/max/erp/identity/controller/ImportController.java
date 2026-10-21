package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.AuthorizationService;
import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.ImportExecutionResponse;
import com.max.erp.identity.dto.ImportJobResponse;
import com.max.erp.identity.dto.ImportMappingResponse;
import com.max.erp.identity.dto.ImportMappingSaveRequest;
import com.max.erp.identity.dto.ImportMappingSaveResponse;
import com.max.erp.identity.dto.ImportParseResponse;
import com.max.erp.identity.dto.ImportUploadResponse;
import com.max.erp.identity.dto.ImportValidationResponse;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.service.ImportExecutionService;
import com.max.erp.identity.service.ImportMappingService;
import com.max.erp.identity.service.ImportParserService;
import com.max.erp.identity.service.ImportService;
import com.max.erp.identity.service.ImportValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/configuration/business-objects/{businessObjectId}/imports"
)
public class ImportController {

    private final ImportService importService;
    private final ImportParserService importParserService;
    private final ConfigurationImportJobRepository importJobRepository;
    private final ImportMappingService importMappingService;
    private final ImportValidationService importValidationService;
    private final ImportExecutionService importExecutionService;
    private final BusinessObjectRepository businessObjectRepository;
    private final SecurityAuthorizationGuard authorizationGuard;
    private final AuthorizationService authorizationService;

    @Autowired
    public ImportController(
            ImportService importService,
            ImportParserService importParserService,
            ConfigurationImportJobRepository importJobRepository,
            ImportMappingService importMappingService,
            ImportValidationService importValidationService,
            ImportExecutionService importExecutionService,
            BusinessObjectRepository businessObjectRepository,
            SecurityAuthorizationGuard authorizationGuard,
            AuthorizationService authorizationService) {

        this.importService = importService;
        this.importParserService = importParserService;
        this.importJobRepository = importJobRepository;
        this.importMappingService = importMappingService;
        this.importValidationService = importValidationService;
        this.importExecutionService = importExecutionService;
        this.businessObjectRepository = businessObjectRepository;
        this.authorizationGuard = authorizationGuard;
        this.authorizationService = authorizationService;
    }

    public ImportController(
            ImportService importService,
            ImportParserService importParserService,
            ConfigurationImportJobRepository importJobRepository,
            ImportMappingService importMappingService,
            ImportValidationService importValidationService) {

        this(
                importService,
                importParserService,
                importJobRepository,
                importMappingService,
                importValidationService,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Upload a CSV or XLSX file for import.
     *
     * POST /api/v1/configuration/business-objects/{businessObjectId}/imports
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImportUploadResponse> upload(
            @PathVariable UUID businessObjectId,
            @RequestParam("file") MultipartFile file) {

        ImportUploadResponse response =
                importService.upload(
                        businessObjectId,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Parse an uploaded import file into import rows.
     *
     * POST /api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/parse
     */
    @PostMapping("/{importId}/parse")
    public ResponseEntity<ImportParseResponse> parse(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID importId) {

        ConfigurationImportJob job =
                importJobRepository.findById(importId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Import job not found"
                                ));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new IllegalArgumentException(
                    "Import job does not belong to the specified business object"
            );
        }

        int totalRows =
                importParserService.parse(importId);

        ConfigurationImportJob parsedJob =
                importJobRepository.findById(importId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Import job not found"
                                ));

        ImportParseResponse response =
                new ImportParseResponse();

        response.setImportJobId(parsedJob.getId());
        response.setBusinessObjectId(parsedJob.getBusinessObjectId());
        response.setStatus(parsedJob.getStatus());
        response.setTotalRows(totalRows);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate the parsed import without creating business object records.
     *
     * POST /api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/validate
     */
    @PostMapping("/{importId}/validate")
    public ResponseEntity<ImportValidationResponse> validate(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID importId) {

        ImportValidationResponse response =
                importValidationService.validate(
                        businessObjectId,
                        importId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Return a read-only mapping preview for a parsed import.
     *
     * GET /api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/mapping
     */
    @GetMapping("/{importId}/mapping")
    public ResponseEntity<ImportMappingResponse> mapping(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID importId) {

        ImportMappingResponse response =
                importMappingService.getMappingPreview(
                        businessObjectId,
                        importId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Save the user-confirmed mapping for an import.
     *
     * PUT /api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/mapping
     */
    @PutMapping("/{importId}/mapping")
    public ResponseEntity<ImportMappingSaveResponse> saveMapping(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID importId,
            @RequestBody ImportMappingSaveRequest request) {

        ImportMappingSaveResponse response =
                importMappingService.saveMapping(
                        businessObjectId,
                        importId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Execute the validated import and create business object records.
     *
     * POST /api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/execute
     */
    @PostMapping("/{importId}/execute")
    public ResponseEntity<ImportExecutionResponse> execute(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID importId,
            Authentication authentication) {

        Authentication auth = authentication != null
                ? authentication
                : SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        if (authorizationGuard != null) {
            authorizationGuard.require(auth, null, null, "CONFIGURATION", "MANAGE");
        }

        ConfigurationImportJob job = importJobRepository.findById(importId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import job not found"
                ));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Import job does not belong to the specified business object"
            );
        }

        UUID userId = null;
        if (authorizationService != null) {
            try {
                userId = authorizationService.resolveUserId(auth.getName());
            } catch (Exception ignored) {
                // Keep userId null if resolve fails
            }
        }

        if (importExecutionService == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Import execution service is not configured"
            );
        }

        ImportExecutionResponse response =
                importExecutionService.execute(
                        businessObjectId,
                        importId,
                        userId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get import history for a business object (ordered newest-first).
     *
     * GET /api/v1/configuration/business-objects/{businessObjectId}/imports
     */
    @GetMapping
    public ResponseEntity<List<ImportJobResponse>> getHistory(
            @PathVariable UUID businessObjectId) {

        if (businessObjectRepository != null) {
            businessObjectRepository.findById(businessObjectId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Business object not found"
                            ));
        }

        List<ConfigurationImportJob> jobs =
                importJobRepository.findAllByBusinessObjectIdOrderByCreatedAtDesc(
                        businessObjectId
                );

        List<ImportJobResponse> response =
                jobs.stream()
                        .map(this::toJobResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get import job details.
     *
     * GET /api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}
     */
    @GetMapping("/{importId}")
    public ResponseEntity<ImportJobResponse> getJob(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID importId) {

        ConfigurationImportJob job =
                importJobRepository.findById(importId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Import job not found"
                                ));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Import job does not belong to the specified business object"
            );
        }

        return ResponseEntity.ok(toJobResponse(job));
    }

    private ImportJobResponse toJobResponse(ConfigurationImportJob job) {
        ImportJobResponse response = new ImportJobResponse();
        response.setId(job.getId());
        response.setBusinessObjectId(job.getBusinessObjectId());
        response.setFileName(job.getFileName());
        response.setFileType(job.getFileType());
        response.setFileSizeBytes(job.getFileSizeBytes());
        response.setStatus(job.getStatus());
        response.setTotalRows(job.getTotalRows());
        response.setValidRows(job.getValidRows());
        response.setInvalidRows(job.getInvalidRows());
        response.setDuplicateRows(job.getDuplicateRows());
        response.setProcessedRows(job.getProcessedRows());
        response.setColumnMapping(job.getColumnMapping());
        response.setSummary(job.getSummary());
        response.setCreatedBy(job.getCreatedBy());
        response.setCreatedAt(job.getCreatedAt());
        response.setStartedAt(job.getStartedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}
