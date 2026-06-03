package com.smartiot.qualityinspection.dashboard.service;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.alert.repository.AlertRepository;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.dashboard.dto.DashboardSummaryDto;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.inspection.repository.InspectionResultRepository;
import com.smartiot.qualityinspection.sensor.dto.SensorHealthDto;
import com.smartiot.qualityinspection.sensor.service.SensorHealthService;
import com.smartiot.qualityinspection.simulation.service.SimulationService;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public DashboardService(InspectionResultRepository resultRepository,
                            AlertRepository alertRepository,
                            SimulationService simulationService,
                            SensorHealthService sensorHealthService,
                            RealtimeBroadcaster broadcaster) {
        this.resultRepository = resultRepository;
        this.alertRepository = alertRepository;
        this.simulationService = simulationService;
        this.sensorHealthService = sensorHealthService;
        this.broadcaster = broadcaster;
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
