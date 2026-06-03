package com.smartiot.qualityinspection.report.controller;

import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.report.dto.QualitySummaryReportDto;
import com.smartiot.qualityinspection.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Historical reporting endpoints. Available to any authenticated user. Date filters accept
 * ISO-8601 instants.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/quality-summary")
    public QualitySummaryReportDto qualitySummary(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long simulationRunId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return reportService.qualitySummary(batchId, simulationRunId, parseInstant(from), parseInstant(to));
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Invalid date/time '" + value + "'. Use ISO-8601.");
        }
    }
}
