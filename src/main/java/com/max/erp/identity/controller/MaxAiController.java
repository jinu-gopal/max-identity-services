package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.MaxAiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class MaxAiController {

    private final MaxAiService maxAiService;

    public MaxAiController(MaxAiService maxAiService) {
        this.maxAiService = maxAiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = maxAiService.processChat(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/schema")
    public ResponseEntity<AiGeneratedSchemaDto> generateSchema(@RequestBody Map<String, String> body) {
        String prompt = body.getOrDefault("prompt", "custom entity");
        AiGeneratedSchemaDto schema = maxAiService.generateSchemaFromPrompt(prompt);
        return ResponseEntity.ok(schema);
    }

    @PostMapping("/generate/workflow")
    public ResponseEntity<AiGeneratedWorkflowDto> generateWorkflow(@RequestBody Map<String, String> body) {
        String prompt = body.getOrDefault("prompt", "standard approval");
        String defaultBoKey = body.getOrDefault("businessObjectKey", "customer");
        AiGeneratedWorkflowDto workflow = maxAiService.generateWorkflowFromPrompt(prompt, defaultBoKey);
        return ResponseEntity.ok(workflow);
    }

    @GetMapping("/insights")
    public ResponseEntity<AiInsightsDto> getInsights() {
        AiInsightsDto insights = maxAiService.analyzeOperationalInsights();
        return ResponseEntity.ok(insights);
    }
}
