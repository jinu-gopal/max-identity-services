package com.max.erp.identity.service;

import com.max.erp.identity.dto.ImportUploadResponse;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.enums.ImportJobStatus;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImportService {

    private final BusinessObjectRepository businessObjectRepository;
    private final ConfigurationImportJobRepository importJobRepository;
    private final Path storageRoot;

    public ImportService(
            BusinessObjectRepository businessObjectRepository,
            ConfigurationImportJobRepository importJobRepository,
            @Value("${max.erp.import.storage-directory:./data/imports}")
            String storageDirectory) {

        this.businessObjectRepository = businessObjectRepository;
        this.importJobRepository = importJobRepository;
        this.storageRoot = Paths.get(storageDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public ImportUploadResponse upload(
            UUID businessObjectId,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import file is required"
            );
        }

        BusinessObject businessObject =
                businessObjectRepository.findById(businessObjectId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Business object not found"
                                ));

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import file name is required"
            );
        }

        String safeFileName = sanitizeFileName(originalFileName);
        String fileType = determineFileType(safeFileName);

        ConfigurationImportJob job =
                new ConfigurationImportJob();

        job.setBusinessObjectId(businessObject.getId());
        job.setFileName(safeFileName);
        job.setFileType(fileType);
        job.setFileSizeBytes(file.getSize());
        job.setStatus(ImportJobStatus.UPLOADED);

        ConfigurationImportJob savedJob =
                importJobRepository.save(job);

        Path jobDirectory =
                storageRoot
                        .resolve(savedJob.getId().toString())
                        .resolve("original")
                        .normalize();

        Path targetFile =
                jobDirectory
                        .resolve(safeFileName)
                        .normalize();

        if (!targetFile.startsWith(jobDirectory)) {
            markFailed(savedJob);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid import file name"
            );
        }

        try {
            Files.createDirectories(jobDirectory);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        targetFile
                );
            }

        } catch (IOException exception) {

            markFailed(savedJob);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store import file"
            );
        }

        ImportUploadResponse response =
                new ImportUploadResponse();

        response.setImportJobId(savedJob.getId());
        response.setBusinessObjectId(savedJob.getBusinessObjectId());
        response.setFileName(savedJob.getFileName());
        response.setFileType(savedJob.getFileType());
        response.setFileSizeBytes(savedJob.getFileSizeBytes());
        response.setStatus(savedJob.getStatus());
        response.setCreatedAt(savedJob.getCreatedAt());

        return response;
    }

    private String determineFileType(String fileName) {

        String lowerCaseFileName =
                fileName.toLowerCase(Locale.ROOT);

        if (lowerCaseFileName.endsWith(".csv")) {
            return "CSV";
        }

        if (lowerCaseFileName.endsWith(".xlsx")) {
            return "XLSX";
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only CSV and XLSX files are supported"
        );
    }

    private String sanitizeFileName(String fileName) {

        String normalized =
                Paths.get(fileName)
                        .getFileName()
                        .toString()
                        .trim();

        if (normalized.isBlank()
                || ".".equals(normalized)
                || "..".equals(normalized)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid import file name"
            );
        }

        return normalized;
    }

    private void markFailed(ConfigurationImportJob job) {

        job.setStatus(ImportJobStatus.FAILED);
        importJobRepository.save(job);
    }
}
