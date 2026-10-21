package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NotificationStudioService {

    private static final Logger log = LoggerFactory.getLogger(NotificationStudioService.class);

    private final Map<String, NotificationTemplateDto> templatesMap = new ConcurrentHashMap<>();
    private final List<NotificationDispatchLogDto> dispatchLogs = new CopyOnWriteArrayList<>();

    public NotificationStudioService() {
        initializeDefaultTemplates();
        initializeDefaultLogs();
    }

    private void initializeDefaultTemplates() {
        NotificationTemplateDto tpl1 = new NotificationTemplateDto(
                "tpl-wf-pending",
                "tpl_wf_pending_approval",
                "Workflow Approval Requested",
                "EMAIL",
                "WORKFLOW_PENDING_APPROVAL",
                "Action Required: Workflow Approval for {{record.name}} (Step: {{workflow.step}})",
                "Hello {{recipient.name}},\n\nA new approval request has been assigned to your queue for {{record.businessObject}} [{{record.name}}].\n\nSubmitted By: {{workflow.initiator}}\nAmount / Value: ${{record.amount}}\nNext Step: {{workflow.step}}\n\nPlease review and approve in MAX-ERP: {{system.appUrl}}/configuration/workflows",
                "{{workflow.assignedApproverEmail}}",
                List.of("{{record.name}}", "{{record.businessObject}}", "{{record.amount}}", "{{workflow.step}}", "{{workflow.initiator}}", "{{recipient.name}}", "{{system.appUrl}}"),
                "ACTIVE",
                Instant.now().minusSeconds(120000)
        );
        templatesMap.put(tpl1.getId(), tpl1);

        NotificationTemplateDto tpl2 = new NotificationTemplateDto(
                "tpl-wf-approved-slack",
                "tpl_wf_approved_slack",
                "Workflow Approved Slack Alert",
                "SLACK",
                "WORKFLOW_APPROVED",
                "Workflow Approved: {{record.name}}",
                ":white_check_mark: *Workflow Approved*: The request for *{{record.name}}* has been approved by {{workflow.approver}}.\n> Business Object: {{record.businessObject}}\n> Total Value: ${{record.amount}}\n> Timestamp: {{workflow.completedAt}}",
                "#finance-approvals",
                List.of("{{record.name}}", "{{record.businessObject}}", "{{record.amount}}", "{{workflow.approver}}", "{{workflow.completedAt}}"),
                "ACTIVE",
                Instant.now().minusSeconds(90000)
        );
        templatesMap.put(tpl2.getId(), tpl2);

        NotificationTemplateDto tpl3 = new NotificationTemplateDto(
                "tpl-wf-rejected-email",
                "tpl_wf_rejected_email",
                "Workflow Rejected Notice",
                "EMAIL",
                "WORKFLOW_REJECTED",
                "Decision Notice: Request for {{record.name}} has been REJECTED",
                "Dear {{workflow.initiator}},\n\nYour submitted request for {{record.businessObject}} [{{record.name}}] was rejected by {{workflow.approver}}.\n\nReason for Rejection:\n\"{{workflow.decisionNotes}}\"\n\nYou may modify the record and re-submit for approval in the Workflows Hub.",
                "{{workflow.initiatorEmail}}",
                List.of("{{record.name}}", "{{record.businessObject}}", "{{workflow.initiator}}", "{{workflow.approver}}", "{{workflow.decisionNotes}}"),
                "ACTIVE",
                Instant.now().minusSeconds(60000)
        );
        templatesMap.put(tpl3.getId(), tpl3);

        NotificationTemplateDto tpl4 = new NotificationTemplateDto(
                "tpl-rec-created-inapp",
                "tpl_record_created_inapp",
                "New Record Created In-App Alert",
                "IN_APP",
                "RECORD_CREATED",
                "New {{record.businessObject}} Created",
                "{{user.name}} created a new {{record.businessObject}} [{{record.name}}]. View details in the Operations Hub.",
                "all-subscribers",
                List.of("{{record.name}}", "{{record.businessObject}}", "{{user.name}}"),
                "ACTIVE",
                Instant.now().minusSeconds(40000)
        );
        templatesMap.put(tpl4.getId(), tpl4);

        NotificationTemplateDto tpl5 = new NotificationTemplateDto(
                "tpl-high-val-slack",
                "tpl_high_value_alert_slack",
                "High-Value Transaction Security Alert",
                "SLACK",
                "HIGH_VALUE_TRANSACTION",
                "ALERT: High-Value Order {{record.name}}",
                ":warning: *High-Value Transaction Warning*: Order *{{record.name}}* with amount ${{record.amount}} exceeded standard review threshold. Immediate governance review recommended.",
                "#exec-governance",
                List.of("{{record.name}}", "{{record.amount}}", "{{user.name}}"),
                "ACTIVE",
                Instant.now().minusSeconds(20000)
        );
        templatesMap.put(tpl5.getId(), tpl5);
    }

    private void initializeDefaultLogs() {
        dispatchLogs.add(new NotificationDispatchLogDto(
                "log-dsp-1",
                "tpl_wf_approved_slack",
                "Workflow Approved Slack Alert",
                "SLACK",
                "#finance-approvals",
                "Workflow Approved: ACME Corp Enterprise Renewal",
                ":white_check_mark: *Workflow Approved*: The request for ACME Corp Enterprise Renewal has been approved by Finance Director.",
                "DELIVERED",
                Instant.now().minusSeconds(3600),
                42L,
                Map.of("channel", "slack", "httpStatus", 200)
        ));

        dispatchLogs.add(new NotificationDispatchLogDto(
                "log-dsp-2",
                "tpl_wf_pending_approval",
                "Workflow Approval Requested",
                "EMAIL",
                "vp.finance@enterprise.corp",
                "Action Required: Workflow Approval for Global Logistics Hub",
                "Hello VP Finance, A new approval request has been assigned to your queue...",
                "DELIVERED",
                Instant.now().minusSeconds(7200),
                185L,
                Map.of("provider", "SendGrid", "messageId", "msg-99214-sg")
        ));

        dispatchLogs.add(new NotificationDispatchLogDto(
                "log-dsp-3",
                "tpl_record_created_inapp",
                "New Record Created In-App Alert",
                "IN_APP",
                "user-admin-1",
                "New Customer Created",
                "Sarah Connor created a new Customer [Apex Quantum Technologies].",
                "DELIVERED",
                Instant.now().minusSeconds(14400),
                8L,
                Map.of("feed", "in-app-notifications")
        ));
    }

    public List<NotificationTemplateDto> getAllTemplates(String channel) {
        return templatesMap.values().stream()
                .filter(t -> channel == null || channel.isBlank() || channel.equalsIgnoreCase(t.getChannel()))
                .sorted(Comparator.comparing(NotificationTemplateDto::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public NotificationTemplateDto getTemplateById(String id) {
        return templatesMap.get(id);
    }

    public NotificationTemplateDto createTemplate(CreateNotificationTemplateRequest req) {
        String id = "tpl-" + UUID.randomUUID().toString().substring(0, 8);
        NotificationTemplateDto tpl = new NotificationTemplateDto(
                id,
                req.getTemplateKey(),
                req.getName(),
                req.getChannel().toUpperCase(),
                req.getTriggerEvent().toUpperCase(),
                req.getSubject() != null ? req.getSubject() : req.getName(),
                req.getBody(),
                req.getRecipientExpression() != null ? req.getRecipientExpression() : "admin@enterprise.corp",
                req.getAvailableMergeTags() != null ? req.getAvailableMergeTags() : extractMergeTags(req.getBody() + " " + req.getSubject()),
                req.getStatus() != null ? req.getStatus().toUpperCase() : "ACTIVE",
                Instant.now()
        );
        templatesMap.put(id, tpl);
        log.info("Created notification template: {} ({})", tpl.getName(), tpl.getId());
        return tpl;
    }

    public NotificationTemplateDto updateTemplate(String id, CreateNotificationTemplateRequest req) {
        NotificationTemplateDto existing = templatesMap.get(id);
        if (existing == null) return null;

        existing.setTemplateKey(req.getTemplateKey());
        existing.setName(req.getName());
        existing.setChannel(req.getChannel().toUpperCase());
        existing.setTriggerEvent(req.getTriggerEvent().toUpperCase());
        if (req.getSubject() != null) existing.setSubject(req.getSubject());
        existing.setBody(req.getBody());
        if (req.getRecipientExpression() != null) existing.setRecipientExpression(req.getRecipientExpression());
        if (req.getAvailableMergeTags() != null) {
            existing.setAvailableMergeTags(req.getAvailableMergeTags());
        } else {
            existing.setAvailableMergeTags(extractMergeTags(req.getBody() + " " + req.getSubject()));
        }
        if (req.getStatus() != null) existing.setStatus(req.getStatus().toUpperCase());
        existing.setUpdatedAt(Instant.now());

        return existing;
    }

    public boolean deleteTemplate(String id) {
        return templatesMap.remove(id) != null;
    }

    public TestDispatchNotificationResponse testDispatch(TestDispatchNotificationRequest req) {
        NotificationTemplateDto tpl = req.getTemplateId() != null ? templatesMap.get(req.getTemplateId()) : null;
        if (tpl == null && !templatesMap.isEmpty()) {
            tpl = templatesMap.values().iterator().next();
        }

        Map<String, Object> context = req.getContextData() != null ? req.getContextData() : new HashMap<>();
        // Set realistic fallbacks for merge tags
        context.putIfAbsent("record.name", "Titan Solar Installations PO #8921");
        context.putIfAbsent("record.businessObject", "Sales Order");
        context.putIfAbsent("record.amount", "45,000.00");
        context.putIfAbsent("workflow.step", "Finance Director Approval");
        context.putIfAbsent("workflow.initiator", "Sarah Jenkins");
        context.putIfAbsent("workflow.approver", "David Wallace");
        context.putIfAbsent("workflow.decisionNotes", "Approved in accordance with Q4 capital budget.");
        context.putIfAbsent("workflow.completedAt", Instant.now().toString());
        context.putIfAbsent("recipient.name", "Auditor / Manager");
        context.putIfAbsent("user.name", "Administrator");
        context.putIfAbsent("system.appUrl", "http://localhost:3000");

        String rawSubject = tpl != null ? tpl.getSubject() : "Test Notification Notification";
        String rawBody = tpl != null ? tpl.getBody() : "Hello, this is a test notification.";

        String renderedSubject = interpolate(rawSubject, context);
        String renderedBody = interpolate(rawBody, context);
        String channel = req.getChannel() != null ? req.getChannel() : (tpl != null ? tpl.getChannel() : "EMAIL");
        String recipient = req.getRecipient() != null && !req.getRecipient().isBlank() ? req.getRecipient() :
                (tpl != null ? interpolate(tpl.getRecipientExpression(), context) : "test@enterprise.corp");

        NotificationDispatchLogDto logEntry = new NotificationDispatchLogDto(
                "log-" + UUID.randomUUID().toString().substring(0, 8),
                tpl != null ? tpl.getTemplateKey() : "test_adhoc",
                tpl != null ? tpl.getName() : "Ad-hoc Test Dispatch",
                channel,
                recipient,
                renderedSubject,
                renderedBody.length() > 120 ? renderedBody.substring(0, 117) + "..." : renderedBody,
                "DELIVERED",
                Instant.now(),
                (long) (15 + Math.random() * 45),
                Map.of("simulation", true, "dispatchedBy", "Admin Test Sandbox")
        );

        dispatchLogs.add(0, logEntry);
        // Keep last 100 logs
        if (dispatchLogs.size() > 100) {
            dispatchLogs.remove(dispatchLogs.size() - 1);
        }

        return new TestDispatchNotificationResponse(
                true,
                tpl != null ? tpl.getTemplateKey() : "custom",
                channel,
                recipient,
                renderedSubject,
                renderedBody,
                logEntry
        );
    }

    public List<NotificationDispatchLogDto> getDispatchLogs() {
        return new ArrayList<>(dispatchLogs);
    }

    private String interpolate(String template, Map<String, Object> context) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = entry.getKey();
            String placeholder = "{{" + key + "}}";
            String val = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, val);
        }
        return result;
    }

    private List<String> extractMergeTags(String text) {
        if (text == null) return Collections.emptyList();
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{[a-zA-Z0-9._-]+\\}\\}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String tag = matcher.group();
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
