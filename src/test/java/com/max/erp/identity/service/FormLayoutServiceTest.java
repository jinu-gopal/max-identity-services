package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateFormLayoutRequest;
import com.max.erp.identity.dto.FormLayoutDto;
import com.max.erp.identity.dto.FormLayoutFieldDto;
import com.max.erp.identity.dto.FormLayoutSectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormLayoutServiceTest {

    private FormLayoutService formLayoutService;

    @BeforeEach
    void setUp() {
        formLayoutService = new FormLayoutService();
    }

    @Test
    void testInitialSeedLayoutsPresent() {
        List<FormLayoutDto> layouts = formLayoutService.getAllLayouts(null);
        assertNotNull(layouts);
        assertFalse(layouts.isEmpty());
        assertTrue(layouts.size() >= 3);
    }

    @Test
    void testGetLayoutByIdFound() {
        List<FormLayoutDto> layouts = formLayoutService.getAllLayouts(null);
        FormLayoutDto first = layouts.get(0);

        FormLayoutDto found = formLayoutService.getLayoutById(first.getId());
        assertNotNull(found);
        assertEquals(first.getName(), found.getName());
        assertEquals(first.getBusinessObjectId(), found.getBusinessObjectId());
    }

    @Test
    void testCreateLayout() {
        String boId = "bo-lead-test";
        CreateFormLayoutRequest req = new CreateFormLayoutRequest();
        req.setLayoutKey("layout_lead_test");
        req.setBusinessObjectId(boId);
        req.setBusinessObjectName("Lead");
        req.setName("Custom Lead Layout");
        req.setAssignedRole("SALES_REP");
        req.setDefault(true);

        FormLayoutSectionDto section = new FormLayoutSectionDto(
                "sec-1",
                "General Info",
                "Basic lead details",
                2,
                true,
                true,
                List.of(new FormLayoutFieldDto("lead_source", "Lead Source", "TEXT", true, false, 1, "WEBSITE"))
        );

        req.setSections(List.of(section));

        FormLayoutDto created = formLayoutService.createLayout(req);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Custom Lead Layout", created.getName());
        assertEquals("SALES_REP", created.getAssignedRole());
        assertEquals(1, created.getSections().size());

        List<FormLayoutDto> boLayouts = formLayoutService.getAllLayouts(boId);
        assertEquals(1, boLayouts.size());
    }
}
