package com.smartiot.qualityinspection.inspection.service;

import com.smartiot.qualityinspection.alert.service.AlertService;
import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.event.ProductReadingsCompletedEvent;
import com.smartiot.qualityinspection.dashboard.service.DashboardService;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import com.smartiot.qualityinspection.inspection.repository.InspectionResultRepository;
import com.smartiot.qualityinspection.inspection.service.QualityInspectionEngine.ClassificationOutcome;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.sensor.repository.SensorReadingRepository;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates quality inspection. When a product's readings are complete, it gathers the
 * product-level readings, asks the pure {@link QualityInspectionEngine} for a verdict,
 * stores exactly one {@link InspectionResult}, broadcasts it, raises a failed-product
 * alert when needed, and refreshes the dashboard summary.
 */
@Service
public class InspectionService {

    private static final Logger log = LoggerFactory.getLogger(InspectionService.class);

    private final SensorReadingRepository readingRepository;
    private final ThresholdConfigurationRepository thresholdRepository;
    private final InspectionResultRepository resultRepository;
    private final QualityInspectionEngine engine;
    private final AlertService alertService;
    private final RealtimeBroadcaster broadcaster;
    private final DashboardService dashboardService;

    public InspectionService(SensorReadingRepository readingRepository,
                             ThresholdConfigurationRepository thresholdRepository,
                             InspectionResultRepository resultRepository,
                             QualityInspectionEngine engine,
                             AlertService alertService,
                             RealtimeBroadcaster broadcaster,
                             DashboardService dashboardService) {
        this.readingRepository = readingRepository;
        this.thresholdRepository = thresholdRepository;
        this.resultRepository = resultRepository;
        this.engine = engine;
        this.alertService = alertService;
        this.broadcaster = broadcaster;
        this.dashboardService = dashboardService;
    }

    @EventListener
    public void onProductReadingsCompleted(ProductReadingsCompletedEvent event) {
        String productCode = event.productCode();

        // Exactly one result per product.
        if (resultRepository.findByProductCode(productCode).isPresent()) {
            return;
        }

        List<SensorReading> readings = readingRepository.findByProductCodeOrderByTimestampAsc(productCode);
        SensorReading weight = latestOfType(readings, SensorType.WEIGHT);
        SensorReading camera = latestOfType(readings, SensorType.CAMERA);
        ThresholdConfiguration weightThreshold =
                thresholdRepository.findBySensorType(SensorType.WEIGHT).orElse(null);

        ClassificationOutcome outcome = engine.classify(weight, camera, weightThreshold);

        InspectionResult result = resultRepository.save(new InspectionResult(
                productCode, event.batchId(), event.simulationRunId(),
                outcome.status(), outcome.score(), outcome.explanation(), Instant.now()));

        broadcaster.broadcastInspectionResult(InspectionResultDto.from(result));
        log.debug("Classified {} as {} ({})", productCode, result.getStatus(), result.getScore());

        if (result.getStatus() == QualityStatus.FAIL) {
            alertService.createFailedProductAlert(result);
        }

        dashboardService.broadcastSummary();
    }

    private SensorReading latestOfType(List<SensorReading> readings, SensorType type) {
        SensorReading found = null;
        for (SensorReading reading : readings) {
            if (reading.getSensorType() == type) {
                found = reading; // readings are ordered ascending, so the last wins
            }
        }
        return found;
    }
}
