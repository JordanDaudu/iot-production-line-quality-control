package com.smartiot.qualityinspection.alert.service;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.alert.model.Alert;
import com.smartiot.qualityinspection.alert.repository.AlertRepository;
import com.smartiot.qualityinspection.common.enums.AlertSeverity;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import com.smartiot.qualityinspection.common.enums.AlertType;
import com.smartiot.qualityinspection.common.exception.ResourceNotFoundException;
import com.smartiot.qualityinspection.dashboard.service.DashboardService;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import com.smartiot.qualityinspection.sensor.model.Sensor;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Creates, lists and updates alerts. Every change is broadcast to dashboard clients and
 * triggers a fresh dashboard summary so counters stay in sync.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final RealtimeBroadcaster broadcaster;
    private final DashboardService dashboardService;

    public AlertService(AlertRepository alertRepository,
                        RealtimeBroadcaster broadcaster,
                        DashboardService dashboardService) {
        this.alertRepository = alertRepository;
        this.broadcaster = broadcaster;
        this.dashboardService = dashboardService;
    }

    // ----- Creation -----

    /** Raised when a product is classified as FAIL (FR-15). */
    public Alert createFailedProductAlert(InspectionResult result) {
        Alert alert = baseAlert(AlertType.FAILED_PRODUCT, AlertSeverity.CRITICAL);
        alert.setMessage("Product " + result.getProductCode() + " failed inspection: " + result.getExplanation());
        alert.setSource("Quality Engine");
        alert.setProductCode(result.getProductCode());
        alert.setSimulationRunId(result.getSimulationRunId());
        return persistAndPublish(alert);
    }

    /** Raised when a machine reading exceeds its hard limit (FR-16). */
    public Alert createMaintenanceAlert(SensorReading reading, ThresholdConfiguration threshold) {
        String unit = reading.getUnit() != null ? " " + reading.getUnit() : "";
        Alert alert = baseAlert(AlertType.MAINTENANCE, AlertSeverity.CRITICAL);
        alert.setMessage(String.format(Locale.US,
                "%s reading %.1f%s exceeded the limit of %.1f%s at %s",
                reading.getSensorType(), reading.getValue(), unit,
                threshold.getMaxValue(), unit, reading.getMachineId()));
        alert.setSource(reading.getMachineId());
        alert.setSensorKey(reading.getSensorKey());
        alert.setSimulationRunId(reading.getSimulationRunId());
        return persistAndPublish(alert);
    }

    /** Raised when vibration stays abnormally high over several readings (FR-04, FR-16). */
    public Alert createSustainedVibrationAlert(SensorReading reading) {
        Alert alert = baseAlert(AlertType.MAINTENANCE, AlertSeverity.WARNING);
        alert.setMessage(String.format(Locale.US,
                "Sustained abnormal vibration at %s (recent readings consistently above the warning level)",
                reading.getMachineId()));
        alert.setSource(reading.getMachineId());
        alert.setSensorKey(reading.getSensorKey());
        alert.setSimulationRunId(reading.getSimulationRunId());
        return persistAndPublish(alert);
    }

    /** Raised when a sensor stops sending data (FR-17). */
    public Alert createSensorHealthAlert(Sensor sensor) {
        Alert alert = baseAlert(AlertType.SENSOR_HEALTH, AlertSeverity.WARNING);
        alert.setMessage("Sensor " + sensor.getSensorKey() + " (" + sensor.getSensorType() + ") went offline");
        alert.setSource(sensor.getSensorKey());
        alert.setSensorKey(sensor.getSensorKey());
        return persistAndPublish(alert);
    }

    // ----- Listing & lifecycle (FR-24) -----

    public List<AlertDto> listAlerts(AlertStatus status) {
        List<Alert> alerts = (status == null)
                ? alertRepository.findTop100ByOrderByCreatedAtDesc()
                : alertRepository.findByStatusOrderByCreatedAtDesc(status);
        return alerts.stream().map(AlertDto::from).toList();
    }

    public AlertDto acknowledge(Long alertId, String actor, String note) {
        Alert alert = requireAlert(alertId);
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(actor);
        alert.setAcknowledgedAt(Instant.now());
        if (note != null && !note.isBlank()) {
            alert.setNote(note);
        }
        return publishUpdate(alert);
    }

    public AlertDto resolve(Long alertId, String actor) {
        Alert alert = requireAlert(alertId);
        if (alert.getAcknowledgedBy() == null) {
            alert.setAcknowledgedBy(actor);
            alert.setAcknowledgedAt(Instant.now());
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        return publishUpdate(alert);
    }

    // ----- Internals -----

    private Alert baseAlert(AlertType type, AlertSeverity severity) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setCreatedAt(Instant.now());
        return alert;
    }

    private Alert requireAlert(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("No alert found with id " + alertId));
    }

    private Alert persistAndPublish(Alert alert) {
        Alert saved = alertRepository.save(alert);
        broadcaster.broadcastAlert(AlertDto.from(saved));
        dashboardService.broadcastSummary();
        log.info("Alert created: {} [{}] - {}", saved.getType(), saved.getSeverity(), saved.getMessage());
        return saved;
    }

    private AlertDto publishUpdate(Alert alert) {
        Alert saved = alertRepository.save(alert);
        AlertDto dto = AlertDto.from(saved);
        broadcaster.broadcastAlert(dto);
        dashboardService.broadcastSummary();
        log.info("Alert {} -> {} by {}", saved.getId(), saved.getStatus(), saved.getAcknowledgedBy());
        return dto;
    }
}
