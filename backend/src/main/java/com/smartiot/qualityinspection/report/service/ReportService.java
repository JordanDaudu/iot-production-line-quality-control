package com.smartiot.qualityinspection.report.service;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import com.smartiot.qualityinspection.inspection.repository.InspectionResultRepository;
import com.smartiot.qualityinspection.report.dto.QualitySummaryReportDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Builds historical quality reports from persisted inspection results, with optional
 * filters by batch, simulation run and time range (FR-21).
 */
@Service
public class ReportService {

    private final InspectionResultRepository resultRepository;

    public ReportService(InspectionResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public QualitySummaryReportDto qualitySummary(Long batchId, Long simulationRunId, Instant from, Instant to) {
        List<InspectionResult> results = resultRepository.search(null, batchId, simulationRunId, from, to);

        long pass = results.stream().filter(r -> r.getStatus() == QualityStatus.PASS).count();
        long warning = results.stream().filter(r -> r.getStatus() == QualityStatus.WARNING).count();
        long fail = results.stream().filter(r -> r.getStatus() == QualityStatus.FAIL).count();
        long total = results.size();

        return new QualitySummaryReportDto(
                total, pass, warning, fail,
                rate(pass, total), rate(warning, total), rate(fail, total));
    }

    private double rate(long count, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((count * 1000.0) / total) / 10.0;
    }
}
