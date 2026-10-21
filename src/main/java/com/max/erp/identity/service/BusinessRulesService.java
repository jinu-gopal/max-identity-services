package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessRuleDto;
import com.max.erp.identity.dto.CreateBusinessRuleRequest;
import com.max.erp.identity.dto.EvaluateRulesRequest;
import com.max.erp.identity.dto.EvaluateRulesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BusinessRulesService {

    private static final Logger log = LoggerFactory.getLogger(BusinessRulesService.class);

    private final Map<String, BusinessRuleDto> rulesMap = new ConcurrentHashMap<>();

    public BusinessRulesService() {
        initializeDefaultRules();
    }

    private void initializeDefaultRules() {
        // Customer - Positive Credit Limit Validation
        saveRuleInternal(new BusinessRuleDto(
                "rule-cust-credit-val",
                "customer_credit_limit_positive",
                "Customer Credit Limit Must Be Non-Negative",
                "customer-bo-id",
                "Customer",
                "VALIDATION",
                "ON_SUBMIT",
                "creditLimit >= 0",
                Map.of("errorMessage", "Credit limit cannot be a negative amount.", "field", "creditLimit"),
                "ACTIVE",
                10,
                Instant.now().minusSeconds(86400)
        ));

        // Customer - Tax Exemption Conditional Visibility
        saveRuleInternal(new BusinessRuleDto(
                "rule-cust-tax-vis",
                "customer_tax_exemption_visibility",
                "Show Tax Exemption Reason Only When Tax Exempt",
                "customer-bo-id",
                "Customer",
                "VISIBILITY",
                "ON_CHANGE",
                "isTaxExempt == true",
                Map.of("targetField", "taxExemptReason", "visibleWhenTrue", true),
                "ACTIVE",
                20,
                Instant.now().minusSeconds(80000)
        ));

        // Customer - Lock Tax ID When Active
        saveRuleInternal(new BusinessRuleDto(
                "rule-cust-lock-active",
                "customer_lock_tax_id_active",
                "Lock Tax ID and Legal Name Once Customer Is Active",
                "customer-bo-id",
                "Customer",
                "LOCKING",
                "ON_LOAD",
                "status == 'ACTIVE'",
                Map.of("targetFields", List.of("taxId", "legalName", "registrationNumber")),
                "ACTIVE",
                30,
                Instant.now().minusSeconds(75000)
        ));

        // Sales Order - Credit Limit Validation
        saveRuleInternal(new BusinessRuleDto(
                "rule-so-credit-check",
                "sales_order_credit_limit_check",
                "Enforce Customer Credit Limit On Sales Orders",
                "sales-order-bo-id",
                "Sales Order",
                "VALIDATION",
                "ON_SUBMIT",
                "totalAmount <= creditLimit",
                Map.of("errorMessage", "Sales Order total exceeds customer approved credit limit.", "field", "totalAmount"),
                "ACTIVE",
                10,
                Instant.now().minusSeconds(70000)
        ));

        // Sales Order - Dynamic Net Total Computation
        saveRuleInternal(new BusinessRuleDto(
                "rule-so-net-calc",
                "sales_order_net_total_calc",
                "Calculate Net Total with Discount and Tax",
                "sales-order-bo-id",
                "Sales Order",
                "COMPUTATION",
                "ON_CHANGE",
                "subtotal > 0",
                Map.of("targetField", "netTotal", "formula", "subtotal - discountAmount + taxAmount"),
                "ACTIVE",
                15,
                Instant.now().minusSeconds(65000)
        ));

        // Vendor - Email Format Validation
        saveRuleInternal(new BusinessRuleDto(
                "rule-vnd-email-val",
                "vendor_email_required",
                "Require Valid Corporate Email For Vendors",
                "vendor-bo-id",
                "Vendor",
                "VALIDATION",
                "ON_SUBMIT",
                "email != null && email.contains('@')",
                Map.of("errorMessage", "A valid corporate vendor contact email is required.", "field", "email"),
                "ACTIVE",
                10,
                Instant.now().minusSeconds(60000)
        ));

        // Product - Margin Computation
        saveRuleInternal(new BusinessRuleDto(
                "rule-prd-margin-calc",
                "product_margin_computation",
                "Compute Gross Margin Percentage",
                "product-bo-id",
                "Product",
                "COMPUTATION",
                "ON_CHANGE",
                "sellingPrice > 0",
                Map.of("targetField", "marginPercent", "formula", "((sellingPrice - costPrice) / sellingPrice) * 100"),
                "ACTIVE",
                12,
                Instant.now().minusSeconds(55000)
        ));
    }

    private void saveRuleInternal(BusinessRuleDto rule) {
        rulesMap.put(rule.getId(), rule);
    }

    public List<BusinessRuleDto> getAllRules(String businessObjectId) {
        return rulesMap.values().stream()
                .filter(r -> businessObjectId == null || businessObjectId.isBlank() ||
                        businessObjectId.equalsIgnoreCase(r.getBusinessObjectId()) ||
                        businessObjectId.equalsIgnoreCase(r.getBusinessObjectName()))
                .sorted(Comparator.comparingInt(BusinessRuleDto::getPriorityOrder))
                .collect(Collectors.toList());
    }

    public BusinessRuleDto getRuleById(String id) {
        return rulesMap.get(id);
    }

    public BusinessRuleDto createRule(CreateBusinessRuleRequest request) {
        String id = "rule-" + UUID.randomUUID().toString().substring(0, 8);
        BusinessRuleDto rule = new BusinessRuleDto(
                id,
                request.getRuleKey(),
                request.getName(),
                request.getBusinessObjectId() != null ? request.getBusinessObjectId() : "custom-bo",
                request.getBusinessObjectName() != null ? request.getBusinessObjectName() : "Business Object",
                request.getRuleType().toUpperCase(),
                request.getTriggerEvent() != null ? request.getTriggerEvent().toUpperCase() : "ON_CHANGE",
                request.getConditionExpression(),
                request.getActionPayload() != null ? request.getActionPayload() : new HashMap<>(),
                request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE",
                request.getPriorityOrder() > 0 ? request.getPriorityOrder() : (rulesMap.size() + 1) * 10,
                Instant.now()
        );
        rulesMap.put(id, rule);
        log.info("Created business rule: {} ({})", rule.getName(), rule.getId());
        return rule;
    }

    public BusinessRuleDto updateRule(String id, CreateBusinessRuleRequest request) {
        BusinessRuleDto existing = rulesMap.get(id);
        if (existing == null) {
            return null;
        }
        existing.setName(request.getName());
        existing.setRuleKey(request.getRuleKey());
        if (request.getBusinessObjectId() != null) existing.setBusinessObjectId(request.getBusinessObjectId());
        if (request.getBusinessObjectName() != null) existing.setBusinessObjectName(request.getBusinessObjectName());
        if (request.getRuleType() != null) existing.setRuleType(request.getRuleType().toUpperCase());
        if (request.getTriggerEvent() != null) existing.setTriggerEvent(request.getTriggerEvent().toUpperCase());
        if (request.getConditionExpression() != null) existing.setConditionExpression(request.getConditionExpression());
        if (request.getActionPayload() != null) existing.setActionPayload(request.getActionPayload());
        if (request.getStatus() != null) existing.setStatus(request.getStatus().toUpperCase());
        if (request.getPriorityOrder() > 0) existing.setPriorityOrder(request.getPriorityOrder());

        return existing;
    }

    public BusinessRuleDto toggleRule(String id) {
        BusinessRuleDto rule = rulesMap.get(id);
        if (rule == null) return null;
        rule.setStatus("ACTIVE".equalsIgnoreCase(rule.getStatus()) ? "INACTIVE" : "ACTIVE");
        return rule;
    }

    public boolean deleteRule(String id) {
        return rulesMap.remove(id) != null;
    }

    public EvaluateRulesResponse evaluateRules(EvaluateRulesRequest request) {
        EvaluateRulesResponse response = new EvaluateRulesResponse();
        Map<String, Object> data = request.getRecordData() != null ? request.getRecordData() : new HashMap<>();
        String boId = request.getBusinessObjectId();
        String boKey = request.getBusinessObjectKey();

        List<BusinessRuleDto> activeRules = rulesMap.values().stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> {
                    if (boId != null && !boId.isBlank() && boId.equalsIgnoreCase(r.getBusinessObjectId())) return true;
                    if (boKey != null && !boKey.isBlank() && (boKey.equalsIgnoreCase(r.getBusinessObjectName()) ||
                            r.getBusinessObjectName().toLowerCase().contains(boKey.toLowerCase()))) return true;
                    return (boId == null || boId.isBlank()) && (boKey == null || boKey.isBlank());
                })
                .sorted(Comparator.comparingInt(BusinessRuleDto::getPriorityOrder))
                .collect(Collectors.toList());

        for (BusinessRuleDto rule : activeRules) {
            try {
                boolean conditionMatched = evaluateCondition(rule.getConditionExpression(), data);
                response.getEvaluationLogs().add(String.format("Rule [%s]: Condition '%s' evaluated to %s",
                        rule.getRuleKey(), rule.getConditionExpression(), conditionMatched));

                if (conditionMatched) {
                    response.getAppliedRuleKeys().add(rule.getRuleKey());

                    switch (rule.getRuleType()) {
                        case "VALIDATION":
                            // If condition matched for validation, check if it defines a violation or required pass
                            // If condition is e.g. "creditLimit >= 0", then conditionMatched==true means valid.
                            // If condition is like "totalAmount > creditLimit", it triggers error.
                            // To make rules intuitive:
                            handleValidationAction(rule, data, response);
                            break;

                        case "VISIBILITY":
                            handleVisibilityAction(rule, conditionMatched, response);
                            break;

                        case "LOCKING":
                            handleLockingAction(rule, response);
                            break;

                        case "COMPUTATION":
                            handleComputationAction(rule, data, response);
                            break;
                    }
                } else {
                    // For visibility, if condition failed, hide targetField
                    if ("VISIBILITY".equalsIgnoreCase(rule.getRuleType())) {
                        Object targetField = rule.getActionPayload() != null ? rule.getActionPayload().get("targetField") : null;
                        if (targetField != null) {
                            response.getFieldVisibility().put(targetField.toString(), false);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Rule evaluation error on {}: {}", rule.getRuleKey(), e.getMessage());
                response.getEvaluationLogs().add(String.format("Rule [%s] evaluation warning: %s", rule.getRuleKey(), e.getMessage()));
            }
        }

        response.setValid(response.getValidationErrors().isEmpty());
        return response;
    }

    private void handleValidationAction(BusinessRuleDto rule, Map<String, Object> data, EvaluateRulesResponse response) {
        // If rule specifies errorMessage and condition failed, or condition explicitly checks error condition
        String expr = rule.getConditionExpression() != null ? rule.getConditionExpression().trim() : "";
        Map<String, Object> payload = rule.getActionPayload() != null ? rule.getActionPayload() : Map.of();
        String errorMsg = (String) payload.getOrDefault("errorMessage", "Validation rule violated: " + rule.getName());

        // Conventions:
        // If condition expression has "<=" or ">=" or "!=", it is an integrity assertion.
        // If the assertion failed, we'd log error. Since evaluateCondition returned true for this condition,
        // let's check if the rule is an "assert" or "violation_trigger".
        if (rule.getRuleKey().contains("check") || rule.getRuleKey().contains("limit")) {
            // E.g. "totalAmount <= creditLimit": if condition matched, it is valid!
            // But if condition is e.g. "totalAmount > creditLimit", then match means error.
            if (expr.contains(">") && !expr.contains(">=")) {
                response.getValidationErrors().add(errorMsg);
            }
        }
    }

    private void handleVisibilityAction(BusinessRuleDto rule, boolean conditionMatched, EvaluateRulesResponse response) {
        Map<String, Object> payload = rule.getActionPayload() != null ? rule.getActionPayload() : Map.of();
        Object targetField = payload.get("targetField");
        if (targetField != null) {
            response.getFieldVisibility().put(targetField.toString(), conditionMatched);
        }
    }

    private void handleLockingAction(BusinessRuleDto rule, EvaluateRulesResponse response) {
        Map<String, Object> payload = rule.getActionPayload() != null ? rule.getActionPayload() : Map.of();
        Object targetFields = payload.get("targetFields");
        if (targetFields instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) targetFields;
            for (String field : list) {
                response.getFieldLocked().put(field, true);
            }
        } else if (payload.get("targetField") != null) {
            response.getFieldLocked().put(payload.get("targetField").toString(), true);
        }
    }

    private void handleComputationAction(BusinessRuleDto rule, Map<String, Object> data, EvaluateRulesResponse response) {
        Map<String, Object> payload = rule.getActionPayload() != null ? rule.getActionPayload() : Map.of();
        String targetField = (String) payload.get("targetField");
        String formula = (String) payload.get("formula");
        if (targetField == null) return;

        double subtotal = getDoubleValue(data, "subtotal", 0.0);
        double discount = getDoubleValue(data, "discountAmount", 0.0);
        double tax = getDoubleValue(data, "taxAmount", 0.0);
        double sellingPrice = getDoubleValue(data, "sellingPrice", 0.0);
        double costPrice = getDoubleValue(data, "costPrice", 0.0);
        double quantity = getDoubleValue(data, "quantity", 1.0);
        double unitPrice = getDoubleValue(data, "unitPrice", 0.0);

        if ("netTotal".equalsIgnoreCase(targetField)) {
            double net = subtotal - discount + tax;
            response.getComputedFields().put(targetField, Math.round(net * 100.0) / 100.0);
        } else if ("marginPercent".equalsIgnoreCase(targetField)) {
            if (sellingPrice > 0) {
                double margin = ((sellingPrice - costPrice) / sellingPrice) * 100.0;
                response.getComputedFields().put(targetField, Math.round(margin * 10.0) / 10.0);
            }
        } else if ("totalAmount".equalsIgnoreCase(targetField)) {
            double total = quantity * unitPrice;
            response.getComputedFields().put(targetField, Math.round(total * 100.0) / 100.0);
        } else {
            response.getComputedFields().put(targetField, "Computed by " + rule.getRuleKey());
        }
    }

    private boolean evaluateCondition(String expression, Map<String, Object> data) {
        if (expression == null || expression.isBlank()) return true;
        expression = expression.trim();

        // Check for boolean flag equality: e.g. "isTaxExempt == true"
        if (expression.contains("==")) {
            String[] parts = expression.split("==");
            String field = parts[0].trim();
            String expected = parts[1].trim().replace("'", "").replace("\"", "");
            Object actual = data.get(field);
            return actual != null && String.valueOf(actual).equalsIgnoreCase(expected);
        }

        // Check for inequality: e.g. "status != 'INACTIVE'"
        if (expression.contains("!=")) {
            String[] parts = expression.split("!=");
            String field = parts[0].trim();
            String expected = parts[1].trim().replace("'", "").replace("\"", "");
            Object actual = data.get(field);
            if (expected.equalsIgnoreCase("null")) {
                return actual != null && !String.valueOf(actual).isBlank();
            }
            return actual == null || !String.valueOf(actual).equalsIgnoreCase(expected);
        }

        // Check for numeric comparison: ">="
        if (expression.contains(">=")) {
            String[] parts = expression.split(">=");
            double left = parseOperand(parts[0].trim(), data);
            double right = parseOperand(parts[1].trim(), data);
            return left >= right;
        }

        // Check for numeric comparison: "<="
        if (expression.contains("<=")) {
            String[] parts = expression.split("<=");
            double left = parseOperand(parts[0].trim(), data);
            double right = parseOperand(parts[1].trim(), data);
            return left <= right;
        }

        // Check for numeric comparison: ">"
        if (expression.contains(">")) {
            String[] parts = expression.split(">");
            double left = parseOperand(parts[0].trim(), data);
            double right = parseOperand(parts[1].trim(), data);
            return left > right;
        }

        // Check for numeric comparison: "<"
        if (expression.contains("<")) {
            String[] parts = expression.split("<");
            double left = parseOperand(parts[0].trim(), data);
            double right = parseOperand(parts[1].trim(), data);
            return left < right;
        }

        // String contains: e.g. "email.contains('@')"
        if (expression.contains(".contains(")) {
            String field = expression.substring(0, expression.indexOf(".contains(")).trim();
            if (field.contains("&&")) {
                field = field.substring(field.lastIndexOf("&&") + 2).trim();
            }
            String sub = expression.substring(expression.indexOf("(") + 1, expression.indexOf(")"))
                    .replace("'", "").replace("\"", "").trim();
            Object val = data.get(field);
            return val != null && String.valueOf(val).contains(sub);
        }

        return true;
    }

    private double parseOperand(String token, Map<String, Object> data) {
        if (data.containsKey(token)) {
            return getDoubleValue(data, token, 0.0);
        }
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double getDoubleValue(Map<String, Object> data, String key, double defaultVal) {
        Object v = data.get(key);
        if (v == null) return defaultVal;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
