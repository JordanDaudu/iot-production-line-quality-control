package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.event.ProductReadingsCompletedEvent;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingMessage;
import com.smartiot.qualityinspection.simulation.model.Product;
import com.smartiot.qualityinspection.simulation.repository.ProductRepository;
import com.smartiot.qualityinspection.simulation.service.FaultInjectionService;
import com.smartiot.qualityinspection.simulation.service.SimulationService;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

/**
 * Virtual IoT sensor simulator. While a simulation is RUNNING, each scheduled tick runs
 * one production cycle: a new product (barcode), product-level readings (weight, camera)
 * and machine-level readings (temperature, vibration). Every reading is submitted through
 * {@link IngestionService} — the same validated inbound channel an external sensor client
 * would use.
 *
 * <p>Injected faults are applied here: spike/defect faults force the next reading into the
 * fail band, and a disconnected sensor simply stops emitting so the health monitor detects
 * it as offline.
 */
@Service
public class SensorSimulationService {

    private static final Logger log = LoggerFactory.getLogger(SensorSimulationService.class);

    private static final String MACHINE_ID = "STATION-1";
    private static final String[] DEFECT_CATEGORIES = {
            "OK", "OK", "OK", "OK", "OK", "SCRATCH", "CRACK", "MISSING_LABEL", "UNKNOWN"
    };

    private final SimulationService simulationService;
    private final IngestionService ingestionService;
    private final ProductRepository productRepository;
    private final ThresholdConfigurationRepository thresholdRepository;
    private final FaultInjectionService faultInjectionService;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    public SensorSimulationService(SimulationService simulationService,
                                   IngestionService ingestionService,
                                   ProductRepository productRepository,
                                   ThresholdConfigurationRepository thresholdRepository,
                                   FaultInjectionService faultInjectionService,
                                   ApplicationEventPublisher eventPublisher) {
        this.simulationService = simulationService;
        this.ingestionService = ingestionService;
        this.productRepository = productRepository;
        this.thresholdRepository = thresholdRepository;
        this.faultInjectionService = faultInjectionService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${simulation.tick-interval-ms:3000}")
    public void tick() {
        if (!simulationService.isRunning()) {
            return;
        }
        Long runId = simulationService.getCurrentRunId();
        Long batchId = simulationService.getCurrentBatchId();
        if (runId == null || batchId == null) {
            return;
        }

        // Consume any one-shot faults for this cycle.
        boolean overweight = faultInjectionService.consumeOverweight();
        boolean visualDefect = faultInjectionService.consumeVisualDefect();
        boolean tempSpike = faultInjectionService.consumeTemperatureSpike();
        boolean vibrationSpike = faultInjectionService.consumeVibrationSpike();

        Instant now = Instant.now();
        String productCode = simulationService.nextProductCode();
        productRepository.save(new Product(productCode, batchId, runId, now));

        // Product-level readings.
        emitIfConnected(barcodeReading(productCode, batchId, runId, now));
        emitIfConnected(weightReading(productCode, batchId, runId, now, overweight));
        emitIfConnected(cameraReading(productCode, batchId, runId, now, visualDefect));

        // Machine-level readings.
        emitIfConnected(temperatureReading(runId, now, tempSpike));
        emitIfConnected(vibrationReading(runId, now, vibrationSpike));

        // Signal that this product's readings are complete so it can be classified.
        eventPublisher.publishEvent(new ProductReadingsCompletedEvent(productCode, batchId, runId));
    }

    private void emitIfConnected(SensorReadingMessage message) {
        if (faultInjectionService.isDisconnected(message.sensorKey())) {
            return; // sensor is "disconnected" — skip emitting so it goes offline
        }
        try {
            ingestionService.ingest(message);
        } catch (Exception ex) {
            log.warn("Failed to ingest {} reading: {}", message.sensorType(), ex.getMessage());
        }
    }

    // ----- Reading builders -----

    private SensorReadingMessage barcodeReading(String productCode, Long batchId, Long runId, Instant now) {
        return new SensorReadingMessage(SensorType.BARCODE, "BARCODE-1", productCode, null,
                batchId, runId, null, null, null, null, now);
    }

    private SensorReadingMessage weightReading(String productCode, Long batchId, Long runId, Instant now, boolean forceFail) {
        double value = forceFail ? failValue(SensorType.WEIGHT, 110.0) : numericValue(SensorType.WEIGHT, 95.0, 105.0, 110.0);
        return new SensorReadingMessage(SensorType.WEIGHT, "WEIGHT-1", productCode, null,
                batchId, runId, value, "g", null, null, now);
    }

    private SensorReadingMessage cameraReading(String productCode, Long batchId, Long runId, Instant now, boolean forceDefect) {
        String defect = forceDefect ? "CRACK" : DEFECT_CATEGORIES[random.nextInt(DEFECT_CATEGORIES.length)];
        double confidence = forceDefect ? uniform(30, 60) : ("OK".equals(defect) ? uniform(85, 100) : uniform(55, 95));
        return new SensorReadingMessage(SensorType.CAMERA, "CAMERA-1", productCode, null,
                batchId, runId, null, null, defect, round1(confidence), now);
    }

    private SensorReadingMessage temperatureReading(Long runId, Instant now, boolean forceSpike) {
        double value = forceSpike ? failValue(SensorType.TEMPERATURE, 35.0) : numericValue(SensorType.TEMPERATURE, 18.0, 30.0, 35.0);
        return new SensorReadingMessage(SensorType.TEMPERATURE, "TEMPERATURE-1", null, MACHINE_ID,
                null, runId, value, "C", null, null, now);
    }

    private SensorReadingMessage vibrationReading(Long runId, Instant now, boolean forceSpike) {
        double value = forceSpike ? failValue(SensorType.VIBRATION, 8.0) : numericValue(SensorType.VIBRATION, 0.0, 5.0, 8.0);
        return new SensorReadingMessage(SensorType.VIBRATION, "VIBRATION-1", null, MACHINE_ID,
                null, runId, value, "mm/s", null, null, now);
    }

    // ----- Value generation -----

    private double numericValue(SensorType type, double defWarnMin, double defWarnMax, double defMax) {
        double warnMin = defWarnMin;
        double warnMax = defWarnMax;
        double max = defMax;

        ThresholdConfiguration threshold = thresholdRepository.findBySensorType(type).orElse(null);
        if (threshold != null) {
            warnMin = threshold.getWarnMinValue();
            warnMax = threshold.getWarnMaxValue();
            max = threshold.getMaxValue();
        }

        double roll = random.nextDouble();
        double value;
        if (roll < 0.78) {
            value = uniform(warnMin, warnMax);                                  // PASS band
        } else if (roll < 0.93) {
            value = uniform(warnMax, max);                                      // WARNING band
        } else {
            value = uniform(max, max + Math.max(5.0, max - warnMax));           // FAIL band
        }
        return round1(value);
    }

    /** A value above the hard limit, used for injected faults (guaranteed fail band). */
    private double failValue(SensorType type, double defMax) {
        double max = thresholdRepository.findBySensorType(type)
                .map(ThresholdConfiguration::getMaxValue).orElse(defMax);
        return round1(max + uniform(5.0, 12.0));
    }

    private double uniform(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
