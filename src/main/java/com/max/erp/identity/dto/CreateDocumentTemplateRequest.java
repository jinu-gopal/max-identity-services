package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class CreateDocumentTemplateRequest {

    @NotBlank(message = "Template key is required")
    private String templateKey;

    @NotBlank(message = "Document name is required")
    private String name;

    private String documentType = "INVOICE";
    private String businessObjectId;
    private String businessObjectName;
    private String companyHeader = "MAX ENTERPRISE CORP";
    private String companyAddress = "100 Enterprise Way, Suite 400, San Francisco CA 94107";
    private String primaryColor = "#2563eb";

    @NotBlank(message = "Template HTML is required")
    private String bodyHtml;

    private String footerText = "Thank you for your business. Payment terms net 30 days.";
    private List<String> availableMergeTags = new ArrayList<>();
    private String status = "ACTIVE";

    public CreateDocumentTemplateRequest() {}

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
}
