package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.DocumentGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentGenerationController {

    private final DocumentGenerationService documentService;

    public DocumentGenerationController(DocumentGenerationService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/templates")
    public ResponseEntity<List<DocumentTemplateDto>> getAllTemplates() {
        return ResponseEntity.ok(documentService.getAllTemplates());
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<DocumentTemplateDto> getTemplateById(@PathVariable String id) {
        DocumentTemplateDto tpl = documentService.getTemplateById(id);
        if (tpl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tpl);
    }

    @PostMapping("/templates")
    public ResponseEntity<DocumentTemplateDto> createTemplate(
            @Valid @RequestBody CreateDocumentTemplateRequest request
    ) {
        DocumentTemplateDto created = documentService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<DocumentTemplateDto> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody CreateDocumentTemplateRequest request
    ) {
        DocumentTemplateDto updated = documentService.updateTemplate(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        boolean deleted = documentService.deleteTemplate(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/render")
    public ResponseEntity<RenderDocumentResponse> renderDocument(
            @RequestBody RenderDocumentRequest request
    ) {
        RenderDocumentResponse response = documentService.renderDocument(request);
        return ResponseEntity.ok(response);
    }
}
