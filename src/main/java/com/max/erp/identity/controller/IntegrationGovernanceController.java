package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.IntegrationGovernanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationGovernanceController {

    private final IntegrationGovernanceService integrationService;

    public IntegrationGovernanceController(IntegrationGovernanceService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<IntegrationSummaryDto> getSummary() {
        return ResponseEntity.ok(integrationService.getIntegrationSummary());
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ApiClientDto>> getClients() {
        return ResponseEntity.ok(integrationService.getApiClients());
    }

    @PostMapping("/clients")
    public ResponseEntity<ApiClientDto> createClient(@Valid @RequestBody CreateApiClientRequest request) {
        ApiClientDto created = integrationService.createApiClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable String id) {
        boolean deleted = integrationService.deleteApiClient(id);
        return ResponseEntity.ok(Map.of("success", deleted, "id", id));
    }

    @GetMapping("/webhooks")
    public ResponseEntity<List<WebhookSubscriptionDto>> getWebhooks() {
        return ResponseEntity.ok(integrationService.getWebhooks());
    }

    @PostMapping("/webhooks")
    public ResponseEntity<WebhookSubscriptionDto> createWebhook(@Valid @RequestBody CreateWebhookRequest request) {
        WebhookSubscriptionDto created = integrationService.createWebhook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/webhooks/{id}")
    public ResponseEntity<Map<String, Object>> deleteWebhook(@PathVariable String id) {
        boolean deleted = integrationService.deleteWebhook(id);
        return ResponseEntity.ok(Map.of("success", deleted, "id", id));
    }

    @PostMapping("/webhooks/{id}/test")
    public ResponseEntity<WebhookDeliveryLogDto> testPingWebhook(@PathVariable String id) {
        WebhookDeliveryLogDto log = integrationService.testPingWebhook(id);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/connectors")
    public ResponseEntity<List<ConnectorDto>> getConnectors() {
        return ResponseEntity.ok(integrationService.getConnectors());
    }

    @PostMapping("/connectors/{id}/toggle")
    public ResponseEntity<ConnectorDto> toggleConnector(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean enable = body.getOrDefault("enable", true);
        ConnectorDto updated = integrationService.toggleConnector(id, enable);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<WebhookDeliveryLogDto>> getDeliveries() {
        return ResponseEntity.ok(integrationService.getDeliveryLogs());
    }
}
