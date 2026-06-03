package com.smartiot.qualityinspection.dashboard.controller;

import com.smartiot.qualityinspection.dashboard.dto.DashboardSummaryDto;
import com.smartiot.qualityinspection.dashboard.dto.TelemetryPointDto;
import com.smartiot.qualityinspection.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides the dashboard snapshot used to initialise the UI on load. Live updates after
 * that arrive over {@code /topic/dashboard-summary}.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryDto summary() {
        return dashboardService.buildSummary();
    }

    @GetMapping("/telemetry")
    public List<TelemetryPointDto> telemetry(@RequestParam(defaultValue = "500") int limit) {
        return dashboardService.getTelemetry(limit);
    }
}
