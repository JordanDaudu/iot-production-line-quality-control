package com.smartiot.qualityinspection.dashboard.controller;

import com.smartiot.qualityinspection.dashboard.dto.DashboardSummaryDto;
import com.smartiot.qualityinspection.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
