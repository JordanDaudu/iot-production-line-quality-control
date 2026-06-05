package com.smartiot.qualityinspection.dashboard.controller;

import com.smartiot.qualityinspection.dashboard.dto.DashboardSummaryDto;
import com.smartiot.qualityinspection.dashboard.dto.DefectCountDto;
import com.smartiot.qualityinspection.dashboard.dto.SpcChartDto;
import com.smartiot.qualityinspection.dashboard.dto.TelemetryPointDto;
import com.smartiot.qualityinspection.dashboard.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides the dashboard snapshot used to initialise the UI on load. Live updates after
 * that arrive over {@code /topic/dashboard-summary}.
 *
 * <p>The summary and telemetry feed the Dashboard (open to all authenticated roles). The
 * SPC and defect-Pareto endpoints back the Quality page, which is restricted to Quality
 * Managers, Operators and Administrators (NFR-11).
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

    @GetMapping("/spc")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'OPERATOR', 'QUALITY_MANAGER')")
    public SpcChartDto spc(@RequestParam(defaultValue = "100") int limit) {
        return dashboardService.getSpcChart(limit);
    }

    @GetMapping("/defect-pareto")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'OPERATOR', 'QUALITY_MANAGER')")
    public List<DefectCountDto> defectPareto() {
        return dashboardService.getDefectPareto();
    }
}
