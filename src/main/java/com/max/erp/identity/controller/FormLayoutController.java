package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateFormLayoutRequest;
import com.max.erp.identity.dto.FormLayoutDto;
import com.max.erp.identity.service.FormLayoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuration/layouts")
public class FormLayoutController {

    private final FormLayoutService formLayoutService;

    public FormLayoutController(FormLayoutService formLayoutService) {
        this.formLayoutService = formLayoutService;
    }

    @GetMapping
    public ResponseEntity<List<FormLayoutDto>> getAllLayouts(
            @RequestParam(required = false) String businessObjectId
    ) {
        return ResponseEntity.ok(formLayoutService.getAllLayouts(businessObjectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormLayoutDto> getLayoutById(@PathVariable String id) {
        FormLayoutDto layout = formLayoutService.getLayoutById(id);
        if (layout == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(layout);
    }

    @PostMapping
    public ResponseEntity<FormLayoutDto> createLayout(@Valid @RequestBody CreateFormLayoutRequest request) {
        FormLayoutDto created = formLayoutService.createLayout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormLayoutDto> updateLayout(
            @PathVariable String id,
            @Valid @RequestBody CreateFormLayoutRequest request
    ) {
        FormLayoutDto updated = formLayoutService.updateLayout(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLayout(@PathVariable String id) {
        boolean deleted = formLayoutService.deleteLayout(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
