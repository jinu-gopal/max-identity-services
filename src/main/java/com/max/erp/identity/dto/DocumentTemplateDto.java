package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DocumentTemplateDto {
    private String id;
    private String templateKey;
    private String name;
    private String documentType; // INVOICE, PURCHASE_ORDER, PACKING_SLIP, APPROVAL_CERTIFICATE
    private String businessObjectId;
    private String businessObjectName;
    private String companyHeader;
    private String companyAddress;
    private String primaryColor;
    private String bodyHtml;
    private String footerText;
    private List<String> availableMergeTags = new ArrayList<>();
    private String status; // ACTIVE, INACTIVE
    private Instant updatedAt;

    public DocumentTemplateDto() {}

    public DocumentTemplateDto(String id, String templateKey, String name, String documentType,
                               String businessObjectId, String businessObjectName,
                               String companyHeader, String companyAddress, String primaryColor,
                               String bodyHtml, String footerText, List<String> availableMergeTags,
                               String status, Instant updatedAt) {
        this.id = id;
        this.templateKey = templateKey;
        this.name = name;
        this.documentType = documentType;
        this.businessObjectId = businessObjectId;
        this.businessObjectName = businessObjectName;
        this.companyHeader = companyHeader;
        this.companyAddress = companyAddress;
        this.primaryColor = primaryColor;
        this.bodyHtml = bodyHtml;
        this.footerText = footerText;
        this.availableMergeTags = availableMergeTags != null ? availableMergeTags : new ArrayList<>();
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(String businessObjectId) { this.businessObjectId = businessObjectId; }

    public String getBusinessObjectName() { return businessObjectName; }
    public void setBusinessObjectName(String businessObjectName) { this.businessObjectName = businessObjectName; }

    public String getCompanyHeader() { return companyHeader; }
    public void setCompanyHeader(String companyHeader) { this.companyHeader = companyHeader; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public String getFooterText() { return footerText; }
    public void setFooterText(String footerText) { this.footerText = footerText; }

    public List<String> getAvailableMergeTags() { return availableMergeTags; }
    public void setAvailableMergeTags(List<String> availableMergeTags) { this.availableMergeTags = availableMergeTags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
