package com.aircargo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "API Catalog", description = "API discovery endpoint for BI tools and external integrations")
public class CatalogController {

    @GetMapping
    @Operation(summary = "API Catalog", description = "Returns the full catalog of available API endpoints with metadata for BI tools and external integrations")
    public ResponseEntity<List<Map<String, Object>>> getCatalog() {
        List<Map<String, Object>> catalog = new ArrayList<>();

        catalog.add(serviceEntry("auth", "Authentication", "/api/auth", List.of(
            endpoint("POST", "/login", "Login with email and password", List.of(), "None"),
            endpoint("POST", "/set-password", "Set or change password", List.of(), "JWT"),
            endpoint("POST", "/change-password", "Change password with MFA", List.of(), "JWT"),
            endpoint("GET", "/me", "Get current user profile", List.of(), "JWT"),
            endpoint("POST", "/refresh", "Refresh access token", List.of(), "Refresh Token"),
            endpoint("GET", "/heartbeat", "Session heartbeat", List.of(), "JWT")
        )));

        catalog.add(serviceEntry("users", "User Management", "/api/users", List.of(
            endpoint("GET", "/", "List all users", List.of("airlineId"), "ADMIN, SUPER_USER"),
            endpoint("GET", "/{id}", "Get user by ID", List.of(), "ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create user", List.of(), "ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update user", List.of(), "ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete user", List.of(), "ADMIN, SUPER_USER"),
            endpoint("GET", "/connected", "List connected users", List.of(), "Any authenticated"),
            endpoint("POST", "/{id}/reset-password", "Reset user password", List.of(), "ADMIN, SUPER_USER"),
            endpoint("POST", "/{id}/mfa/setup", "Setup MFA", List.of(), "ADMIN, SUPER_USER"),
            endpoint("POST", "/{id}/mfa/enable", "Enable MFA", List.of(), "ADMIN, SUPER_USER"),
            endpoint("POST", "/{id}/mfa/disable", "Disable MFA", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("flights", "Flight Operations", "/api/flights", List.of(
            endpoint("GET", "/", "List flights", List.of("airlineId", "date", "status", "flightNumber", "page", "size"), "OPERATIONS, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("GET", "/{id}", "Get flight by ID", List.of(), "OPERATIONS, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create flight", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update flight", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete flight", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("airlines", "Airline Management", "/api/airlines", List.of(
            endpoint("GET", "/", "List all airlines", List.of(), "READ_ONLY+"),
            endpoint("GET", "/{id}", "Get airline by ID", List.of(), "READ_ONLY+"),
            endpoint("POST", "/", "Create airline", List.of(), "ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update airline", List.of(), "ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete airline", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("bookings", "Cargo Bookings", "/api/bookings", List.of(
            endpoint("GET", "/", "List bookings", List.of("airlineId", "flightId", "page", "size"), "TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("GET", "/{id}", "Get booking by ID", List.of(), "TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create booking", List.of(), "TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update booking", List.of(), "TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete booking", List.of(), "ADMIN, SUPER_USER"),
            endpoint("PATCH", "/{id}/awb", "Update AWB number", List.of(), "TRAFFIC, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("mawbs", "Master Air Waybills", "/api/cargo/mawbs", List.of(
            endpoint("GET", "/", "List all MAWBs", List.of("page", "size"), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("GET", "/flight/{flightId}", "List MAWBs by flight", List.of("page", "size"), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create MAWB", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{mawbId}", "Update MAWB", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("PATCH", "/{mawbId}/status", "Update MAWB status", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("GET", "/{mawbId}/supporting-docs", "Get supporting docs", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{mawbId}/supporting-docs", "Update supporting docs", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("GET", "/{mawbId}/supporting-docs/pdf", "Generate evidence PDF", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("hawbs", "House Air Waybills", "/api/cargo/hawbs", List.of(
            endpoint("GET", "/mawb/{mawbId}", "List HAWBs for MAWB", List.of("page", "size"), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create HAWB", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{hawbId}", "Update HAWB", List.of(), "OPERATIONS, TRAFFIC, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("warehouse", "Warehouse Receipts", "/api/warehouse/receipts", List.of(
            endpoint("POST", "/emit", "Emit warehouse receipt", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{receiptId}", "Update receipt", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("POST", "/validate", "Validate receipt (dry-run)", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{receiptId}/pieces", "Get receipt pieces", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{receiptId}/supporting-docs", "Get supporting docs JSON", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{receiptId}/supporting-docs/html", "Get supporting docs HTML", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{receiptId}/supporting-docs/pdf", "Get supporting docs PDF", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{receiptId}/export", "Download receipt Excel", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{receiptId}/pdf", "Download receipt PDF", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("receipts", "Receipt Management", "/api/receipts", List.of(
            endpoint("GET", "/", "List receipts", List.of("airlineId"), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/{id}", "Get receipt by ID", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create receipt", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update receipt", List.of(), "WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete receipt", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("ulds", "Unit Load Devices", "/api/ulds", List.of(
            endpoint("GET", "/", "List ULDs", List.of("airlineId", "flightId", "page", "size"), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("GET", "/{id}", "Get ULD by ID", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create ULD", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update ULD", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("PATCH", "/{id}", "Partial update ULD", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("PATCH", "/{id}/flight", "Assign ULD to flight", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("POST", "/{uldId}/transfer", "Transfer ULD between flights", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete ULD", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("uld-awbs", "ULD-AWB Assignments", "/api/uld-awbs", List.of(
            endpoint("GET", "/", "List ULD-AWB links", List.of("uldId", "mawbId"), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create ULD-AWB link", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update link", List.of(), "OPERATIONS, ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete link", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("scan", "Barcode Scanning", "/api/scan", List.of(
            endpoint("GET", "/lookup", "Lookup barcode (MAWB/HAWB/ULD)", List.of("code"), "OPERATIONS, TRAFFIC, LOAD_PLANNER, WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("POST", "/piece", "Register scanned piece", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("DELETE", "/piece/last", "Undo last scanned piece", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, WAREHOUSE_ASSISTANT, ADMIN, SUPER_USER"),
            endpoint("GET", "/events/{flightId}", "SSE stream for scan events", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("load-planning", "Load Planning", "/api/load-planning", List.of(
            endpoint("GET", "/flight/{flightId}", "Get load plan by flight", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("POST", "/flight/{flightId}/close", "Close/finalize load plan", List.of(), "OPERATIONS, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("POST", "/flight/{flightId}/upload-manifest", "Upload ramp manifest Excel", List.of(), "OPERATIONS, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("GET", "/flight/{flightId}/export-manifest", "Download load plan XLSX", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER"),
            endpoint("GET", "/flight/{flightId}/pallet-sheets", "Download pallet sheets PDF", List.of(), "OPERATIONS, TRAFFIC, LOAD_PLANNER, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("tracking", "MAWB Tracking", "/api/tracking", List.of(
            endpoint("GET", "/mawb/{mawbId}", "Get MAWB timeline", List.of(), "ADMIN, SUPER_USER"),
            endpoint("GET", "/mawbs", "Get all MAWB tracking summary", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("exports", "Data Exports", "/api/exports", List.of(
            endpoint("GET", "/{type}", "Export entity data (flights/bookings/mawbs/receipts/ulds)", List.of("format", "dateFrom", "dateTo", "audit"), "READ_ONLY+"),
            endpoint("POST", "/import/load-planning", "Import load planning from Excel", List.of(), "OPERATIONS, ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("bi", "Business Intelligence", "/api/bi", List.of(
            endpoint("GET", "/dashboard", "Dashboard KPIs", List.of(), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/summary", "Aggregated summary by date range", List.of("dateFrom", "dateTo"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/flights", "Flights BI data", List.of("dateFrom", "dateTo"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/bookings", "Bookings BI data", List.of("dateFrom", "dateTo"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/mawbs", "MAWBs BI data", List.of(), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/receipts", "Receipts BI data", List.of(), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/ulds", "ULDs BI data", List.of(), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/daily", "Daily aggregated data", List.of("dateFrom", "dateTo"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/by-location", "Metrics grouped by origin location", List.of(), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/timeline", "Daily timeline aggregates", List.of("dateFrom", "dateTo"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/top-mawbs", "Top MAWBs by weight", List.of("limit"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/flight-performance", "Flight utilization metrics", List.of("dateFrom", "dateTo"), "ADMIN, SUPER_USER, BI_USER")
        )));

        catalog.add(serviceEntry("reports", "Downloadable Reports", "/api/reports", List.of(
            endpoint("GET", "/daily", "Daily operations report", List.of("date", "format"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/weekly", "Weekly operations report", List.of("weekStart", "format"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/flights", "Flights report export", List.of("dateFrom", "dateTo", "format"), "ADMIN, SUPER_USER, BI_USER"),
            endpoint("GET", "/by-location", "Location-based report", List.of("format"), "ADMIN, SUPER_USER, BI_USER")
        )));

        catalog.add(serviceEntry("compliance", "DUA/Customs Compliance", "/api/compliance", List.of(
            endpoint("GET", "/", "List all DUA records", List.of(), "ADMIN, SUPER_USER"),
            endpoint("GET", "/mawb/{mawbId}", "Get DUA by MAWB", List.of(), "ADMIN, SUPER_USER"),
            endpoint("GET", "/{id}", "Get DUA by ID", List.of(), "ADMIN, SUPER_USER"),
            endpoint("POST", "/", "Create DUA record", List.of(), "ADMIN, SUPER_USER"),
            endpoint("PUT", "/{id}", "Update DUA record", List.of(), "ADMIN, SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete DUA record", List.of(), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("audit", "Audit Logs", "/api/audit-logs", List.of(
            endpoint("GET", "/", "List audit logs", List.of("userId"), "ADMIN, SUPER_USER")
        )));

        catalog.add(serviceEntry("sites", "Site Management", "/api/sites", List.of(
            endpoint("GET", "/", "List all active sites", List.of(), "READ_ONLY+"),
            endpoint("GET", "/{id}", "Get site by ID", List.of(), "READ_ONLY+"),
            endpoint("POST", "/", "Create site", List.of(), "SUPER_USER"),
            endpoint("PUT", "/{id}", "Update site", List.of(), "SUPER_USER"),
            endpoint("DELETE", "/{id}", "Delete site", List.of(), "SUPER_USER")
        )));

        catalog.add(serviceEntry("role-permissions", "Role Permissions", "/api/role-permissions", List.of(
            endpoint("GET", "/views", "List all view permissions", List.of(), "SUPER_USER"),
            endpoint("GET", "/", "List all roles with permissions", List.of(), "SUPER_USER"),
            endpoint("GET", "/{role}", "Get permissions for role", List.of(), "SUPER_USER"),
            endpoint("PUT", "/{role}", "Update role permissions", List.of(), "SUPER_USER"),
            endpoint("POST", "/views", "Create view permission", List.of(), "SUPER_USER"),
            endpoint("PUT", "/views/{id}", "Update view permission", List.of(), "SUPER_USER"),
            endpoint("DELETE", "/views/{id}", "Delete view permission", List.of(), "SUPER_USER")
        )));

        catalog.add(serviceEntry("files", "File Management", "/api/files", List.of(
            endpoint("POST", "/upload", "Upload file (multipart)", List.of(), "ADMIN, SUPER_USER"),
            endpoint("GET", "/{filename}", "Download/serve file", List.of(), "ADMIN, SUPER_USER")
        )));

        return ResponseEntity.ok(catalog);
    }

    private Map<String, Object> serviceEntry(String service, String description, String basePath, List<Map<String, Object>> endpoints) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("service", service);
        entry.put("description", description);
        entry.put("basePath", basePath);
        entry.put("endpoints", endpoints);
        return entry;
    }

    private Map<String, Object> endpoint(String method, String path, String description, List<String> params, String auth) {
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", method);
        ep.put("path", path);
        ep.put("description", description);
        ep.put("parameters", params);
        ep.put("auth", auth);
        return ep;
    }
}
