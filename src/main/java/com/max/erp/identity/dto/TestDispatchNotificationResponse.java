package com.max.erp.identity.dto;

public class TestDispatchNotificationResponse {
    private boolean success;
    private String templateKey;
    private String channel;
    private String recipient;
    private String renderedSubject;
    private String renderedBody;
    private NotificationDispatchLogDto dispatchLog;

    public TestDispatchNotificationResponse() {}

    public TestDispatchNotificationResponse(boolean success, String templateKey, String channel,
                                            String recipient, String renderedSubject,
                                            String renderedBody, NotificationDispatchLogDto dispatchLog) {
        this.success = success;
        this.templateKey = templateKey;
        this.channel = channel;
        this.recipient = recipient;
        this.renderedSubject = renderedSubject;
        this.renderedBody = renderedBody;
        this.dispatchLog = dispatchLog;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getRenderedSubject() { return renderedSubject; }
    public void setRenderedSubject(String renderedSubject) { this.renderedSubject = renderedSubject; }

    public String getRenderedBody() { return renderedBody; }
    public void setRenderedBody(String renderedBody) { this.renderedBody = renderedBody; }

    public NotificationDispatchLogDto getDispatchLog() { return dispatchLog; }
    public void setDispatchLog(NotificationDispatchLogDto dispatchLog) { this.dispatchLog = dispatchLog; }
}
