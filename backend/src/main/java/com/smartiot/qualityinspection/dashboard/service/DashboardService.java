package com.smartiot.qualityinspection.dashboard.service;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.alert.repository.AlertRepository;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.dashboard.dto.DashboardSummaryDto;
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
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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

    public DashboardService(InspectionResultRepository resultRepository,
                            AlertRepository alertRepository,
                            SimulationService simulationService,
                            SensorHealthService sensorHealthService,
                            RealtimeBroadcaster broadcaster,
                            SensorReadingRepository readingRepository,
                            SimulationRunRepository runRepository) {
        this.resultRepository = resultRepository;
        this.alertRepository = alertRepository;
        this.simulationService = simulationService;
        this.sensorHealthService = sensorHealthService;
        this.broadcaster = broadcaster;
        this.readingRepository = readingRepository;
        this.runRepository = runRepository;
    }

    /**
     * Machine telemetry (temperature + vibration paired by tick) for the active run from
     * the start, so the dashboard trend chart can show the whole run rather than only a
     * sliding window. Limited to the most recent {@code limit} points as a safety bound.
     */
    public List<TelemetryPointDto> getTelemetry(int limit) {
        Long runId = simulationService.getCurrentRunId();
        if (runId == null) {
            runId = runRepository.findFirstByOrderByStartedAtDesc().map(SimulationRun::getId).orElse(null);
        }
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
