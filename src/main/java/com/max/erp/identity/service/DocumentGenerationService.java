package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DocumentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentGenerationService.class);

    private final Map<String, DocumentTemplateDto> templatesMap = new ConcurrentHashMap<>();

    public DocumentGenerationService() {
        initializeDefaultTemplates();
    }

    private void initializeDefaultTemplates() {
        DocumentTemplateDto inv = new DocumentTemplateDto(
                "doc-tpl-invoice",
                "doc_commercial_invoice",
                "Enterprise Commercial Tax Invoice",
                "INVOICE",
                "sales-order-bo-id",
                "Sales Order",
                "MAX ENTERPRISE GLOBAL TECHNOLOGIES",
                "100 Enterprise Boulevard, Suite 500, San Francisco CA 94107",
                "#2563eb",
                """
                <div class="invoice-container">
                  <div class="invoice-header">
                    <h2>COMMERCIAL TAX INVOICE</h2>
                    <p><strong>Invoice Number:</strong> {{invoice.number}}</p>
                    <p><strong>Invoice Date:</strong> {{invoice.date}}</p>
                    <p><strong>Due Date:</strong> {{invoice.dueDate}}</p>
                  </div>
                  <div class="billing-grid">
                    <div>
                      <h4>BILLED TO:</h4>
                      <p><strong>{{customer.name}}</strong></p>
                      <p>{{customer.email}}</p>
                      <p>Tax ID: {{customer.taxId}}</p>
                    </div>
                    <div>
                      <h4>PAYMENT TERMS:</h4>
                      <p>{{invoice.paymentTerms}}</p>
                      <p>Status: <strong>{{invoice.status}}</strong></p>
                    </div>
                  </div>
                  <table class="line-items-table">
                    <thead>
                      <tr>
                        <th>Item Description</th>
                        <th>Qty</th>
                        <th>Unit Price</th>
                        <th>Tax</th>
                        <th>Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {{lineItems.rows}}
                    </tbody>
                  </table>
                  <div class="totals-section">
                    <p>Subtotal: <strong>${{invoice.subtotal}}</strong></p>
                    <p>Estimated Tax: <strong>${{invoice.taxAmount}}</strong></p>
                    <h3 class="grand-total">Total Due: ${{invoice.totalAmount}} USD</h3>
                  </div>
                </div>
                """,
                "Thank you for choosing MAX ERP. All payments are governed by Master Enterprise Agreement #MEA-9941.",
                List.of("{{invoice.number}}", "{{invoice.date}}", "{{invoice.dueDate}}", "{{customer.name}}", "{{customer.email}}", "{{customer.taxId}}", "{{invoice.subtotal}}", "{{invoice.taxAmount}}", "{{invoice.totalAmount}}"),
                "ACTIVE",
                Instant.now().minusSeconds(120000)
        );
        templatesMap.put(inv.getId(), inv);

        DocumentTemplateDto po = new DocumentTemplateDto(
                "doc-tpl-po",
                "doc_purchase_order",
                "Official Purchase Order Requisition",
                "PURCHASE_ORDER",
                "vendor-bo-id",
                "Vendor",
                "MAX ENTERPRISE SUPPLY CHAIN DIV",
                "450 Industrial Parkway, Austin TX 78701",
                "#059669",
                """
                <div class="po-container">
                  <div class="invoice-header">
                    <h2>OFFICIAL PURCHASE ORDER</h2>
                    <p><strong>PO Reference:</strong> {{po.number}}</p>
                    <p><strong>Order Date:</strong> {{po.date}}</p>
                  </div>
                  <div class="billing-grid">
                    <div>
                      <h4>VENDOR:</h4>
                      <p><strong>{{vendor.name}}</strong></p>
                      <p>{{vendor.email}}</p>
                    </div>
                    <div>
                      <h4>DELIVERY ADDRESS:</h4>
                      <p>MAX ERP Central Distribution Hub 4</p>
                      <p>Dock Doors 12-14, Austin TX</p>
                    </div>
                  </div>
                  <table class="line-items-table">
                    <thead>
                      <tr><th>Item / SKU</th><th>Qty</th><th>Expected Rate</th><th>Amount</th></tr>
                    </thead>
                    <tbody>
                      {{lineItems.rows}}
                    </tbody>
                  </table>
                  <div class="totals-section">
                    <h3 class="grand-total">Authorized Total: ${{po.totalAmount}}</h3>
                  </div>
                </div>
                """,
                "Deliveries subject to MAX Vendor Quality Standard ISO-9001. Contact procurement@enterprise.corp with questions.",
                List.of("{{po.number}}", "{{po.date}}", "{{vendor.name}}", "{{po.totalAmount}}"),
                "ACTIVE",
                Instant.now().minusSeconds(80000)
        );
        templatesMap.put(po.getId(), po);
    }

    public List<DocumentTemplateDto> getAllTemplates() {
        return templatesMap.values().stream()
                .sorted(Comparator.comparing(DocumentTemplateDto::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public DocumentTemplateDto getTemplateById(String id) {
        return templatesMap.get(id);
    }

    public DocumentTemplateDto createTemplate(CreateDocumentTemplateRequest req) {
        String id = "doc-tpl-" + UUID.randomUUID().toString().substring(0, 8);
        DocumentTemplateDto dto = new DocumentTemplateDto(
                id,
                req.getTemplateKey(),
                req.getName(),
                req.getDocumentType() != null ? req.getDocumentType().toUpperCase() : "INVOICE",
                req.getBusinessObjectId() != null ? req.getBusinessObjectId() : "bo-id",
                req.getBusinessObjectName() != null ? req.getBusinessObjectName() : "Business Object",
                req.getCompanyHeader(),
                req.getCompanyAddress(),
                req.getPrimaryColor() != null ? req.getPrimaryColor() : "#2563eb",
                req.getBodyHtml(),
                req.getFooterText(),
                req.getAvailableMergeTags(),
                req.getStatus() != null ? req.getStatus().toUpperCase() : "ACTIVE",
                Instant.now()
        );
        templatesMap.put(id, dto);
        log.info("Created document template: {} ({})", dto.getName(), dto.getId());
        return dto;
    }

    public DocumentTemplateDto updateTemplate(String id, CreateDocumentTemplateRequest req) {
        DocumentTemplateDto existing = templatesMap.get(id);
        if (existing == null) return null;

        existing.setTemplateKey(req.getTemplateKey());
        existing.setName(req.getName());
        existing.setDocumentType(req.getDocumentType().toUpperCase());
        if (req.getCompanyHeader() != null) existing.setCompanyHeader(req.getCompanyHeader());
        if (req.getCompanyAddress() != null) existing.setCompanyAddress(req.getCompanyAddress());
        if (req.getPrimaryColor() != null) existing.setPrimaryColor(req.getPrimaryColor());
        existing.setBodyHtml(req.getBodyHtml());
        if (req.getFooterText() != null) existing.setFooterText(req.getFooterText());
        if (req.getStatus() != null) existing.setStatus(req.getStatus().toUpperCase());
        existing.setUpdatedAt(Instant.now());

        return existing;
    }

    public boolean deleteTemplate(String id) {
        return templatesMap.remove(id) != null;
    }

    public RenderDocumentResponse renderDocument(RenderDocumentRequest req) {
        DocumentTemplateDto tpl = req.getTemplateId() != null ? templatesMap.get(req.getTemplateId()) : null;
        if (tpl == null && !templatesMap.isEmpty()) {
            tpl = templatesMap.values().iterator().next();
        }

        Map<String, Object> ctx = req.getContextData() != null ? new HashMap<>(req.getContextData()) : new HashMap<>();
        ctx.putIfAbsent("invoice.number", "INV-2026-9810");
        ctx.putIfAbsent("invoice.date", "October 16, 2026");
        ctx.putIfAbsent("invoice.dueDate", "November 15, 2026");
        ctx.putIfAbsent("invoice.paymentTerms", "Net 30 Days via ACH or Wire");
        ctx.putIfAbsent("invoice.status", "APPROVED");
        ctx.putIfAbsent("customer.name", "Acme Industrial Technologies Corp");
        ctx.putIfAbsent("customer.email", "accounts.payable@acme-corp.com");
        ctx.putIfAbsent("customer.taxId", "US-99214-082");
        ctx.putIfAbsent("invoice.subtotal", "44,500.00");
        ctx.putIfAbsent("invoice.taxAmount", "3,750.00");
        ctx.putIfAbsent("invoice.totalAmount", "48,250.00");
        ctx.putIfAbsent("po.number", "PO-2026-5541");
        ctx.putIfAbsent("po.date", "October 16, 2026");
        ctx.putIfAbsent("vendor.name", "Apex Quantum Components Ltd");
        ctx.putIfAbsent("vendor.email", "orders@apexquantum.io");
        ctx.putIfAbsent("po.totalAmount", "48,250.00");

        String rawHtml = tpl != null ? tpl.getBodyHtml() : "<p>Sample Document</p>";

        // Default sample rows if not provided
        String sampleRows = """
            <tr><td>Enterprise ERP Platform Core Licensing (Annual)</td><td>1</td><td>$35,000.00</td><td>$2,800.00</td><td>$37,800.00</td></tr>
            <tr><td>Dedicated Cloud Integration Gateway Add-on</td><td>1</td><td>$6,500.00</td><td>$520.00</td><td>$7,020.00</td></tr>
            <tr><td>Premier 24/7 Enterprise Mission-Critical Support</td><td>1</td><td>$3,000.00</td><td>$430.00</td><td>$3,430.00</td></tr>
            """;
        rawHtml = rawHtml.replace("{{lineItems.rows}}", sampleRows);

        for (Map.Entry<String, Object> entry : ctx.entrySet()) {
            rawHtml = rawHtml.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }

        return new RenderDocumentResponse(
                tpl != null ? tpl.getTemplateKey() : "doc_rendered",
                tpl != null ? tpl.getName() : "Rendered Document",
                rawHtml,
                String.valueOf(ctx.getOrDefault("invoice.totalAmount", "48,250.00")),
                Instant.now()
        );
    }
}
