package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.NotificationStudioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuration/notifications")
public class NotificationStudioController {

    private final NotificationStudioService notificationStudioService;

    public NotificationStudioController(NotificationStudioService notificationStudioService) {
        this.notificationStudioService = notificationStudioService;
    }

    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplateDto>> getAllTemplates(
            @RequestParam(required = false) String channel
    ) {
        return ResponseEntity.ok(notificationStudioService.getAllTemplates(channel));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<NotificationTemplateDto> getTemplateById(@PathVariable String id) {
        NotificationTemplateDto tpl = notificationStudioService.getTemplateById(id);
        if (tpl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tpl);
    }

    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplateDto> createTemplate(
            @Valid @RequestBody CreateNotificationTemplateRequest request
    ) {
        NotificationTemplateDto created = notificationStudioService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<NotificationTemplateDto> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody CreateNotificationTemplateRequest request
    ) {
        NotificationTemplateDto updated = notificationStudioService.updateTemplate(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        boolean deleted = notificationStudioService.deleteTemplate(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test-dispatch")
    public ResponseEntity<TestDispatchNotificationResponse> testDispatch(
            @RequestBody TestDispatchNotificationRequest request
    ) {
        TestDispatchNotificationResponse response = notificationStudioService.testDispatch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<NotificationDispatchLogDto>> getDispatchLogs() {
        return ResponseEntity.ok(notificationStudioService.getDispatchLogs());
    }
}
