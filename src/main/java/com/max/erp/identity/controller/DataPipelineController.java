package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.DataPipelineStudioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/configuration/data-pipeline")
public class DataPipelineController {

    private final DataPipelineStudioService dataPipelineStudioService;

    public DataPipelineController(DataPipelineStudioService dataPipelineStudioService) {
        this.dataPipelineStudioService = dataPipelineStudioService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<DataPipelineMetricsDto> getMetrics() {
        return ResponseEntity.ok(dataPipelineStudioService.getMetrics());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<DataPipelineJobSummaryDto>> getJobs() {
        return ResponseEntity.ok(dataPipelineStudioService.getJobs());
    }

    @GetMapping("/jobs/{id}/errors")
    public ResponseEntity<List<DataPipelineErrorRowDto>> getJobErrors(@PathVariable UUID id) {
        return ResponseEntity.ok(dataPipelineStudioService.getJobErrors(id));
    }

    @PostMapping("/export")
    public ResponseEntity<ExportResponse> exportRecords(@RequestBody ExportRequest request) {
        ExportResponse response = dataPipelineStudioService.exportRecords(request);
        return ResponseEntity.ok(response);
    }
}
