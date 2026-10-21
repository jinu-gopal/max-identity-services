package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessRuleDto;
import com.max.erp.identity.dto.CreateBusinessRuleRequest;
import com.max.erp.identity.dto.EvaluateRulesRequest;
import com.max.erp.identity.dto.EvaluateRulesResponse;
import com.max.erp.identity.service.BusinessRulesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuration/rules")
public class BusinessRulesController {

    private final BusinessRulesService businessRulesService;

    public BusinessRulesController(BusinessRulesService businessRulesService) {
        this.businessRulesService = businessRulesService;
    }

    @GetMapping
    public ResponseEntity<List<BusinessRuleDto>> getAllRules(
            @RequestParam(required = false) String businessObjectId
    ) {
        return ResponseEntity.ok(businessRulesService.getAllRules(businessObjectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessRuleDto> getRuleById(@PathVariable String id) {
        BusinessRuleDto rule = businessRulesService.getRuleById(id);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    @PostMapping
    public ResponseEntity<BusinessRuleDto> createRule(@Valid @RequestBody CreateBusinessRuleRequest request) {
        BusinessRuleDto created = businessRulesService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessRuleDto> updateRule(
            @PathVariable String id,
            @Valid @RequestBody CreateBusinessRuleRequest request
    ) {
        BusinessRuleDto updated = businessRulesService.updateRule(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<BusinessRuleDto> toggleRule(@PathVariable String id) {
        BusinessRuleDto toggled = businessRulesService.toggleRule(id);
        if (toggled == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toggled);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        boolean deleted = businessRulesService.deleteRule(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateRulesResponse> evaluateRules(@RequestBody EvaluateRulesRequest request) {
        EvaluateRulesResponse result = businessRulesService.evaluateRules(request);
        return ResponseEntity.ok(result);
    }
}
