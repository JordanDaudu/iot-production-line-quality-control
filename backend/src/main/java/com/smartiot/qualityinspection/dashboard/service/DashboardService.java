package com.smartiot.qualityinspection.dashboard.service;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.alert.repository.AlertRepository;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.dashboard.dto.DashboardSummaryDto;
import com.smartiot.qualityinspection.dashboard.dto.DefectCountDto;
import com.smartiot.qualityinspection.dashboard.dto.SpcChartDto;
import com.smartiot.qualityinspection.dashboard.dto.TelemetryPointDto;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.inspection.repository.InspectionResultRepository;
import com.smartiot.qualityinspection.sensor.dto.SensorHealthDto;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.sensor.repository.SensorReadingRepository;
import com.smartiot.qualityinspection.sensor.service.SensorHealthService;
import com.smartiot.qualityinspection.simulation.model.SimulationRun;
import com.smartiot.qualityinspection.simulation.repository.SimulationRunRepository;
import com.smartiot.qualityinspection.simulation.service.SimulationService;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the aggregated dashboard snapshot and broadcasts it when state changes. Reads
 * directly from repositories (not other services) so it stays free of dependency cycles.
 */
@Service
public class DashboardService {

    private static final int MAX_ALERTS = 20;

    private final InspectionResultRepository resultRepository;
    private final AlertRepository alertRepository;
    private final SimulationService simulationService;
    private final SensorHealthService sensorHealthService;
    private final RealtimeBroadcaster broadcaster;
    private final SensorReadingRepository readingRepository;
    private final SimulationRunRepository runRepository;
    private final ThresholdConfigurationRepository thresholdRepository;

    public DashboardService(InspectionResultRepository resultRepository,
                            AlertRepository alertRepository,
                            SimulationService simulationService,
                            SensorHealthService sensorHealthService,
                            RealtimeBroadcaster broadcaster,
                            SensorReadingRepository readingRepository,
                            SimulationRunRepository runRepository,
                            ThresholdConfigurationRepository thresholdRepository) {
        this.resultRepository = resultRepository;
        this.alertRepository = alertRepository;
        this.simulationService = simulationService;
        this.sensorHealthService = sensorHealthService;
        this.broadcaster = broadcaster;
        this.readingRepository = readingRepository;
        this.runRepository = runRepository;
        this.thresholdRepository = thresholdRepository;
    }

    /** The active run, or the most recent one if the line is idle. */
    private Long resolveRunId() {
        Long runId = simulationService.getCurrentRunId();
        if (runId == null) {
            runId = runRepository.findFirstByOrderByStartedAtDesc().map(SimulationRun::getId).orElse(null);
        }
        return runId;
    }

    /**
     * Machine telemetry (temperature + vibration paired by tick) for the active run from
     * the start, so the dashboard trend chart can show the whole run rather than only a
     * sliding window. Limited to the most recent {@code limit} points as a safety bound.
     */
    public List<TelemetryPointDto> getTelemetry(int limit) {
        Long runId = resolveRunId();
        if (runId == null) {
            return List.of();
        }

        List<SensorReading> temps =
                readingRepository.findBySimulationRunIdAndSensorTypeOrderByTimestampAsc(runId, SensorType.TEMPERATURE);
        Map<Instant, Double> vibrationByTime = new LinkedHashMap<>();
        readingRepository.findBySimulationRunIdAndSensorTypeOrderByTimestampAsc(runId, SensorType.VIBRATION)
                .forEach(v -> vibrationByTime.put(v.getTimestamp(), v.getValue()));

        List<TelemetryPointDto> points = new ArrayList<>();
        for (SensorReading t : temps) {
            points.add(new TelemetryPointDto(
                    t.getTimestamp() != null ? t.getTimestamp().toString() : null,
                    t.getValue(),
                    vibrationByTime.get(t.getTimestamp())));
        }
        if (points.size() > limit) {
            points = points.subList(points.size() - limit, points.size());
        }
        return points;
    }

    /**
     * Statistical process control data for product weight: the samples in order plus the
     * control limits (mean ± 3σ), centre line and spec limits. Points outside the control
     * limits are flagged out-of-control.
     */
    public SpcChartDto getSpcChart(int limit) {
        Long runId = resolveRunId();
        ThresholdConfiguration weightThreshold =
                thresholdRepository.findBySensorType(SensorType.WEIGHT).orElse(null);
        double specLow = weightThreshold != null ? weightThreshold.getWarnMinValue() : 95.0;
        double specHigh = weightThreshold != null ? weightThreshold.getWarnMaxValue() : 105.0;

        if (runId == null) {
            return new SpcChartDto(List.of(), 0, 0, 0, specLow, specHigh, "g");
        }

        List<SensorReading> weights =
                readingRepository.findBySimulationRunIdAndSensorTypeOrderByTimestampAsc(runId, SensorType.WEIGHT)
                        .stream().filter(r -> r.getValue() != null).toList();
        if (weights.size() > limit) {
            weights = weights.subList(weights.size() - limit, weights.size());
        }
        if (weights.isEmpty()) {
            return new SpcChartDto(List.of(), 0, 0, 0, specLow, specHigh, "g");
        }

        double mean = weights.stream().mapToDouble(SensorReading::getValue).average().orElse(0);
        double variance = weights.stream().mapToDouble(r -> Math.pow(r.getValue() - mean, 2)).average().orElse(0);
        double sigma = Math.sqrt(variance);
        double ucl = round1(mean + 3 * sigma);
        double lcl = round1(mean - 3 * sigma);
        double center = round1(mean);
        String unit = weights.get(0).getUnit() != null ? weights.get(0).getUnit() : "g";

        List<SpcChartDto.SpcPoint> points = new ArrayList<>();
        int index = 1;
        for (SensorReading r : weights) {
            boolean out = r.getValue() > ucl || r.getValue() < lcl;
            points.add(new SpcChartDto.SpcPoint(index++, r.getProductCode(), r.getValue(), out));
        }
        return new SpcChartDto(points, center, ucl, lcl, round1(specLow), round1(specHigh), unit);
    }

    /** Counts of each visual defect category for the active run (Pareto), highest first. */
    public List<DefectCountDto> getDefectPareto() {
        Long runId = resolveRunId();
        if (runId == null) {
            return List.of();
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        readingRepository.findBySimulationRunIdAndSensorTypeOrderByTimestampAsc(runId, SensorType.CAMERA).stream()
                .map(SensorReading::getDefectCategory)
                .filter(d -> d != null && !"OK".equals(d))
                .forEach(d -> counts.merge(d, 1L, Long::sum));

        return counts.entrySet().stream()
                .map(e -> new DefectCountDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(DefectCountDto::count).reversed())
                .toList();
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public DashboardSummaryDto buildSummary() {
        long pass = resultRepository.countByStatus(QualityStatus.PASS);
        long warning = resultRepository.countByStatus(QualityStatus.WARNING);
        long fail = resultRepository.countByStatus(QualityStatus.FAIL);
        long activeAlerts = alertRepository.countByStatus(AlertStatus.ACTIVE);

        List<InspectionResultDto> latestResults = resultRepository.findTop20ByOrderByCreatedAtDesc()
                .stream().map(InspectionResultDto::from).toList();

        List<AlertDto> activeAlertList = alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.ACTIVE)
                .stream().limit(MAX_ALERTS).map(AlertDto::from).toList();

        List<SensorHealthDto> sensors = sensorHealthService.getSensorHealth();

        return new DashboardSummaryDto(
                pass, warning, fail, pass + warning + fail, activeAlerts,
                simulationService.getStatus().state(),
                latestResults, activeAlertList, sensors);
    }

    public void broadcastSummary() {
        broadcaster.broadcastDashboardSummary(buildSummary());
    }
}
