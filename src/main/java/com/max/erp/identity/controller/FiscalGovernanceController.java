package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.FiscalGovernanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/configuration/finance")
public class FiscalGovernanceController {

    private final FiscalGovernanceService fiscalService;

    public FiscalGovernanceController(FiscalGovernanceService fiscalService) {
        this.fiscalService = fiscalService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceConfigSummaryDto> getSummary() {
        return ResponseEntity.ok(fiscalService.getSummary());
    }

    @PutMapping("/currencies/{code}")
    public ResponseEntity<CurrencyExchangeRateDto> updateCurrencyRate(
            @PathVariable String code,
            @RequestBody Map<String, Object> body
    ) {
        double rate = Double.parseDouble(String.valueOf(body.getOrDefault("rateToBase", 1.0)));
        CurrencyExchangeRateDto updated = fiscalService.updateCurrencyRate(code, rate);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/periods/{id}/status")
    public ResponseEntity<FiscalPeriodDto> updatePeriodStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "OPEN");
        FiscalPeriodDto updated = fiscalService.updatePeriodStatus(id, status);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/taxes")
    public ResponseEntity<TaxRuleDto> addTaxRule(@RequestBody TaxRuleDto rule) {
        return ResponseEntity.ok(fiscalService.addTaxRule(rule));
    }
}
