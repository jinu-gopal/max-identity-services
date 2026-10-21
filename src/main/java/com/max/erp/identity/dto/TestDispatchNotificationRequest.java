package com.max.erp.identity.dto;

import java.util.Map;

public class TestDispatchNotificationRequest {
    private String templateId;
    private String channel;
    private String recipient;
    private Map<String, Object> contextData;

    public TestDispatchNotificationRequest() {}

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public Map<String, Object> getContextData() { return contextData; }
    public void setContextData(Map<String, Object> contextData) { this.contextData = contextData; }
}
