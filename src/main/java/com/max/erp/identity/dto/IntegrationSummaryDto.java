package com.max.erp.identity.dto;

import java.util.List;

public class IntegrationSummaryDto {
    private int totalClientsCount;
    private int totalWebhooksCount;
    private int activeConnectorsCount;
    private int successfulDeliveriesCount;
    private int failedDeliveriesCount;
    private List<ApiClientDto> clients;
    private List<WebhookSubscriptionDto> webhooks;
    private List<ConnectorDto> connectors;
    private List<WebhookDeliveryLogDto> recentDeliveries;

    public IntegrationSummaryDto() {}

    public int getTotalClientsCount() { return totalClientsCount; }
    public void setTotalClientsCount(int totalClientsCount) { this.totalClientsCount = totalClientsCount; }

    public int getTotalWebhooksCount() { return totalWebhooksCount; }
    public void setTotalWebhooksCount(int totalWebhooksCount) { this.totalWebhooksCount = totalWebhooksCount; }

    public int getActiveConnectorsCount() { return activeConnectorsCount; }
    public void setActiveConnectorsCount(int activeConnectorsCount) { this.activeConnectorsCount = activeConnectorsCount; }

    public int getSuccessfulDeliveriesCount() { return successfulDeliveriesCount; }
    public void setSuccessfulDeliveriesCount(int successfulDeliveriesCount) { this.successfulDeliveriesCount = successfulDeliveriesCount; }

    public int getFailedDeliveriesCount() { return failedDeliveriesCount; }
    public void setFailedDeliveriesCount(int failedDeliveriesCount) { this.failedDeliveriesCount = failedDeliveriesCount; }

    public List<ApiClientDto> getClients() { return clients; }
    public void setClients(List<ApiClientDto> clients) { this.clients = clients; }

    public List<WebhookSubscriptionDto> getWebhooks() { return webhooks; }
    public void setWebhooks(List<WebhookSubscriptionDto> webhooks) { this.webhooks = webhooks; }

    public List<ConnectorDto> getConnectors() { return connectors; }
    public void setConnectors(List<ConnectorDto> connectors) { this.connectors = connectors; }

    public List<WebhookDeliveryLogDto> getRecentDeliveries() { return recentDeliveries; }
    public void setRecentDeliveries(List<WebhookDeliveryLogDto> recentDeliveries) { this.recentDeliveries = recentDeliveries; }
}
