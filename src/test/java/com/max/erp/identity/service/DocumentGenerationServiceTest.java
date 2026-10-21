package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateDocumentTemplateRequest;
import com.max.erp.identity.dto.DocumentTemplateDto;
import com.max.erp.identity.dto.RenderDocumentRequest;
import com.max.erp.identity.dto.RenderDocumentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentGenerationServiceTest {

    private DocumentGenerationService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentGenerationService();
    }

    @Test
    void testInitialSeedTemplatesPresent() {
        List<DocumentTemplateDto> templates = documentService.getAllTemplates();
        assertNotNull(templates);
        assertTrue(templates.size() >= 2);
    }

    @Test
    void testGetTemplateById() {
        List<DocumentTemplateDto> templates = documentService.getAllTemplates();
        DocumentTemplateDto first = templates.get(0);

        DocumentTemplateDto found = documentService.getTemplateById(first.getId());
        assertNotNull(found);
        assertEquals(first.getName(), found.getName());
    }

    @Test
    void testCreateTemplate() {
        CreateDocumentTemplateRequest req = new CreateDocumentTemplateRequest();
        req.setTemplateKey("doc_bol_standard");
        req.setName("Custom Bill of Lading");
        req.setDocumentType("PACKING_SLIP");
        req.setCompanyHeader("LOGISTICS DEPT");
        req.setBodyHtml("<div class='bol'>BOL #{{bol.number}} for customer {{customer.name}}</div>");

        DocumentTemplateDto created = documentService.createTemplate(req);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Custom Bill of Lading", created.getName());
    }

    @Test
    void testRenderDocumentWithMergeTags() {
        List<DocumentTemplateDto> templates = documentService.getAllTemplates();
        DocumentTemplateDto template = templates.stream()
                .filter(t -> "INVOICE".equals(t.getDocumentType()))
                .findFirst()
                .orElse(templates.get(0));

        RenderDocumentRequest renderReq = new RenderDocumentRequest();
        renderReq.setTemplateId(template.getId());
        renderReq.setContextData(Map.of(
                "invoice.number", "INV-TEST-9999",
                "customer.name", "Acme Test Corp",
                "total.amount", "$12,450.00"
        ));

        RenderDocumentResponse response = documentService.renderDocument(renderReq);
        assertNotNull(response);
        assertNotNull(response.getRenderedHtml());
        assertTrue(response.getRenderedHtml().contains("INV-TEST-9999"));
        assertTrue(response.getRenderedHtml().contains("Acme Test Corp"));
    }
}
