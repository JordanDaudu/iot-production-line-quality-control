package com.smartiot.qualityinspection.report.service;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import com.smartiot.qualityinspection.inspection.repository.InspectionResultRepository;
import com.smartiot.qualityinspection.report.dto.QualitySummaryReportDto;
import com.smartiot.qualityinspection.report.dto.QualitySummaryReportDto.RunBreakdown;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        List<InspectionResult> results = resultRepository.search(null, batchId, simulationRunId, from, to, null);

        long pass = countStatus(results, QualityStatus.PASS);
        long warning = countStatus(results, QualityStatus.WARNING);
        long fail = countStatus(results, QualityStatus.FAIL);
        long total = results.size();

        return new QualitySummaryReportDto(
                total, pass, warning, fail,
                rate(pass, total), rate(warning, total), rate(fail, total),
                breakdownByRun(results));
    }

    private long countStatus(List<InspectionResult> results, QualityStatus status) {
        return results.stream().filter(r -> r.getStatus() == status).count();
    }

    private List<RunBreakdown> breakdownByRun(List<InspectionResult> results) {
        Map<Long, long[]> byRun = new LinkedHashMap<>(); // [total, pass, warning, fail]
        for (InspectionResult r : results) {
            long[] c = byRun.computeIfAbsent(r.getSimulationRunId(), k -> new long[4]);
            c[0]++;
            switch (r.getStatus()) {
                case PASS -> c[1]++;
                case WARNING -> c[2]++;
                case FAIL -> c[3]++;
            }
        }
        List<RunBreakdown> breakdown = new ArrayList<>();
        byRun.forEach((runId, c) -> breakdown.add(new RunBreakdown(runId, c[0], c[1], c[2], c[3])));
        breakdown.sort((a, b) -> Long.compare(b.simulationRunId(), a.simulationRunId()));
        return breakdown;
    }

    private double rate(long count, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((count * 1000.0) / total) / 10.0;
    }
}
