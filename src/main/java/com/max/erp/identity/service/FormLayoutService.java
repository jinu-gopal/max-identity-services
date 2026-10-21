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
public class FormLayoutService {

    private static final Logger log = LoggerFactory.getLogger(FormLayoutService.class);

    private final Map<String, FormLayoutDto> layoutsMap = new ConcurrentHashMap<>();

    public FormLayoutService() {
        initializeDefaultLayouts();
    }

    private void initializeDefaultLayouts() {
        // Customer Default Layout
        FormLayoutSectionDto cSec1 = new FormLayoutSectionDto(
                "c-sec-1",
                "Primary Account Details",
                "Core corporate and contact attributes",
                2,
                true,
                true,
                List.of(
                        new FormLayoutFieldDto("name", "Account Name", "TEXT", true, false, 1, ""),
                        new FormLayoutFieldDto("status", "Account Status", "CHOICE", true, false, 1, "ACTIVE"),
                        new FormLayoutFieldDto("email", "Billing Email", "TEXT", true, false, 1, ""),
                        new FormLayoutFieldDto("phone", "Telephone", "TEXT", false, false, 1, "")
                )
        );

        FormLayoutSectionDto cSec2 = new FormLayoutSectionDto(
                "c-sec-2",
                "Credit & Commercial Terms",
                "Financial parameters, approved credit limits and tax registration",
                2,
                true,
                true,
                List.of(
                        new FormLayoutFieldDto("creditLimit", "Approved Credit Limit", "NUMBER", true, false, 1, "50000"),
                        new FormLayoutFieldDto("tier", "Enterprise Tier", "CHOICE", true, false, 1, "STANDARD"),
                        new FormLayoutFieldDto("isTaxExempt", "Tax Exempt Entity", "BOOLEAN", false, false, 1, "false"),
                        new FormLayoutFieldDto("taxExemptReason", "Tax Exemption Reason", "TEXT", false, false, 1, ""),
                        new FormLayoutFieldDto("taxId", "Federal Tax ID / EIN", "TEXT", false, false, 2, "")
                )
        );

        FormLayoutDto custLayout = new FormLayoutDto(
                "layout-cust-default",
                "layout_customer_master",
                "Customer Standard Master View",
                "customer-bo-id",
                "Customer",
                "ALL",
                true,
                "email",
                List.of("creditLimit", "status", "tier"),
                List.of(cSec1, cSec2),
                List.of("Sales Orders", "Workflow Approvals", "Audit History"),
                Instant.now().minusSeconds(86400)
        );
        layoutsMap.put(custLayout.getId(), custLayout);

        // Sales Order Default Layout
        FormLayoutSectionDto soSec1 = new FormLayoutSectionDto(
                "so-sec-1",
                "Order Header Information",
                "Order references, customer assignment and workflow status",
                2,
                true,
                true,
                List.of(
                        new FormLayoutFieldDto("orderNumber", "Sales Order #", "TEXT", true, true, 1, "SO-AUTO"),
                        new FormLayoutFieldDto("status", "Order Status", "CHOICE", true, false, 1, "DRAFT"),
                        new FormLayoutFieldDto("customer", "Customer Account", "REFERENCE", true, false, 2, "")
                )
        );

        FormLayoutSectionDto soSec2 = new FormLayoutSectionDto(
                "so-sec-2",
                "Financial Summary & Calculations",
                "Subtotal, discounts, calculated taxes, and final net amount",
                2,
                true,
                true,
                List.of(
                        new FormLayoutFieldDto("subtotal", "Subtotal Amount", "NUMBER", true, false, 1, "0.00"),
                        new FormLayoutFieldDto("discountAmount", "Discount Applied", "NUMBER", false, false, 1, "0.00"),
                        new FormLayoutFieldDto("taxAmount", "Tax Total", "NUMBER", false, false, 1, "0.00"),
                        new FormLayoutFieldDto("netTotal", "Net Order Total", "NUMBER", true, true, 1, "0.00")
                )
        );

        FormLayoutDto soLayout = new FormLayoutDto(
                "layout-so-default",
                "layout_sales_order_master",
                "Sales Order Enterprise Layout",
                "sales-order-bo-id",
                "Sales Order",
                "ALL",
                true,
                "customer",
                List.of("netTotal", "status", "orderNumber"),
                List.of(soSec1, soSec2),
                List.of("Order Line Items", "Invoices", "Approval Decision Trail"),
                Instant.now().minusSeconds(60000)
        );
        layoutsMap.put(soLayout.getId(), soLayout);

        // Vendor Layout
        FormLayoutSectionDto vSec1 = new FormLayoutSectionDto(
                "v-sec-1",
                "Vendor Profile & Payment Terms",
                "Supplier registration, corporate email, and banking configuration",
                2,
                true,
                true,
                List.of(
                        new FormLayoutFieldDto("name", "Vendor Name", "TEXT", true, false, 1, ""),
                        new FormLayoutFieldDto("category", "Vendor Category", "CHOICE", true, false, 1, "SUPPLIES"),
                        new FormLayoutFieldDto("email", "Corporate Email", "TEXT", true, false, 1, ""),
                        new FormLayoutFieldDto("paymentTerms", "Default Terms", "CHOICE", false, false, 1, "NET_30")
                )
        );

        FormLayoutDto vendorLayout = new FormLayoutDto(
                "layout-vnd-default",
                "layout_vendor_master",
                "Vendor Supplier Profile",
                "vendor-bo-id",
                "Vendor",
                "ALL",
                true,
                "category",
                List.of("category", "paymentTerms"),
                List.of(vSec1),
                List.of("Purchase Orders", "Contracts"),
                Instant.now().minusSeconds(40000)
        );
        layoutsMap.put(vendorLayout.getId(), vendorLayout);
    }

    public List<FormLayoutDto> getAllLayouts(String boId) {
        return layoutsMap.values().stream()
                .filter(l -> boId == null || boId.isBlank() ||
                        boId.equalsIgnoreCase(l.getBusinessObjectId()) ||
                        boId.equalsIgnoreCase(l.getBusinessObjectName()))
                .sorted(Comparator.comparing(FormLayoutDto::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public FormLayoutDto getLayoutById(String id) {
        return layoutsMap.get(id);
    }

    public FormLayoutDto createLayout(CreateFormLayoutRequest req) {
        String id = "layout-" + UUID.randomUUID().toString().substring(0, 8);
        FormLayoutDto dto = new FormLayoutDto(
                id,
                req.getLayoutKey(),
                req.getName(),
                req.getBusinessObjectId() != null ? req.getBusinessObjectId() : "custom-bo-id",
                req.getBusinessObjectName() != null ? req.getBusinessObjectName() : "Business Object",
                req.getAssignedRole() != null ? req.getAssignedRole() : "ALL",
                req.isDefault(),
                req.getHeaderSubtitleField(),
                req.getHeaderHighlightFields(),
                req.getSections(),
                req.getRelatedLists(),
                Instant.now()
        );
        layoutsMap.put(id, dto);
        log.info("Created Form Layout: {} ({})", dto.getName(), dto.getId());
        return dto;
    }

    public FormLayoutDto updateLayout(String id, CreateFormLayoutRequest req) {
        FormLayoutDto existing = layoutsMap.get(id);
        if (existing == null) return null;

        existing.setLayoutKey(req.getLayoutKey());
        existing.setName(req.getName());
        if (req.getBusinessObjectId() != null) existing.setBusinessObjectId(req.getBusinessObjectId());
        if (req.getBusinessObjectName() != null) existing.setBusinessObjectName(req.getBusinessObjectName());
        if (req.getAssignedRole() != null) existing.setAssignedRole(req.getAssignedRole());
        existing.setDefault(req.isDefault());
        existing.setHeaderSubtitleField(req.getHeaderSubtitleField());
        existing.setHeaderHighlightFields(req.getHeaderHighlightFields());
        existing.setSections(req.getSections());
        existing.setRelatedLists(req.getRelatedLists());
        existing.setUpdatedAt(Instant.now());

        return existing;
    }

    public boolean deleteLayout(String id) {
        return layoutsMap.remove(id) != null;
    }
}
