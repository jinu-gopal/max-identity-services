package com.max.erp.identity.dto;

import java.time.Instant;

public class RenderDocumentResponse {
    private String templateKey;
    private String documentTitle;
    private String renderedHtml;
    private String totalAmount;
    private Instant renderedAt;

    public RenderDocumentResponse() {}

    public RenderDocumentResponse(String templateKey, String documentTitle, String renderedHtml, String totalAmount, Instant renderedAt) {
        this.templateKey = templateKey;
        this.documentTitle = documentTitle;
        this.renderedHtml = renderedHtml;
        this.totalAmount = totalAmount;
        this.renderedAt = renderedAt;
    }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public String getRenderedHtml() { return renderedHtml; }
    public void setRenderedHtml(String renderedHtml) { this.renderedHtml = renderedHtml; }

    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }

    public Instant getRenderedAt() { return renderedAt; }
    public void setRenderedAt(Instant renderedAt) { this.renderedAt = renderedAt; }
}
