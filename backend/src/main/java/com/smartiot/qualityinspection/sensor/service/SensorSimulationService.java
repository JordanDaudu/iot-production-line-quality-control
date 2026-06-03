package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingMessage;
import com.smartiot.qualityinspection.simulation.model.Product;
import com.smartiot.qualityinspection.simulation.repository.ProductRepository;
import com.smartiot.qualityinspection.simulation.service.SimulationService;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

/**
 * Virtual IoT sensor simulator. While a simulation is RUNNING, each scheduled tick runs
 * one production cycle: a new product is created (barcode), product-level readings
 * (weight, camera) are produced for it, and machine-level readings (temperature,
 * vibration) are produced for the station.
 *
 * <p>Every reading is submitted through {@link IngestionService}, i.e. the same validated
 * inbound channel an external sensor client would use. Each emission is isolated so one
 * failing sensor cannot stop the others (NFR-06).
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
    private final Random random = new Random();

    public SensorSimulationService(SimulationService simulationService,
                                   IngestionService ingestionService,
                                   ProductRepository productRepository,
                                   ThresholdConfigurationRepository thresholdRepository) {
        this.simulationService = simulationService;
        this.ingestionService = ingestionService;
        this.productRepository = productRepository;
        this.thresholdRepository = thresholdRepository;
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

        Instant now = Instant.now();
        String productCode = simulationService.nextProductCode();
        productRepository.save(new Product(productCode, batchId, runId, now));

        // Product-level readings.
        emit(barcodeReading(productCode, batchId, runId, now));
        emit(weightReading(productCode, batchId, runId, now));
        emit(cameraReading(productCode, batchId, runId, now));

        // Machine-level readings.
        emit(temperatureReading(runId, now));
        emit(vibrationReading(runId, now));
    }

    private void emit(SensorReadingMessage message) {
        try {
            ingestionService.ingest(message);
        } catch (Exception ex) {
            // Isolate a single sensor failure; the simulation keeps running.
            log.warn("Failed to ingest {} reading: {}", message.sensorType(), ex.getMessage());
        }
    }

    // ----- Reading builders -----

    private SensorReadingMessage barcodeReading(String productCode, Long batchId, Long runId, Instant now) {
        return new SensorReadingMessage(SensorType.BARCODE, "BARCODE-1", productCode, null,
                batchId, runId, null, null, null, null, now);
    }

    private SensorReadingMessage weightReading(String productCode, Long batchId, Long runId, Instant now) {
        double value = numericValue(SensorType.WEIGHT, 95.0, 105.0, 110.0);
        return new SensorReadingMessage(SensorType.WEIGHT, "WEIGHT-1", productCode, null,
                batchId, runId, value, "g", null, null, now);
    }

    private SensorReadingMessage cameraReading(String productCode, Long batchId, Long runId, Instant now) {
        String defect = DEFECT_CATEGORIES[random.nextInt(DEFECT_CATEGORIES.length)];
        double confidence = "OK".equals(defect) ? uniform(85, 100) : uniform(55, 95);
        return new SensorReadingMessage(SensorType.CAMERA, "CAMERA-1", productCode, null,
                batchId, runId, null, null, defect, round1(confidence), now);
    }

    private SensorReadingMessage temperatureReading(Long runId, Instant now) {
        double value = numericValue(SensorType.TEMPERATURE, 18.0, 30.0, 35.0);
        return new SensorReadingMessage(SensorType.TEMPERATURE, "TEMPERATURE-1", null, MACHINE_ID,
                null, runId, value, "C", null, null, now);
    }

    private SensorReadingMessage vibrationReading(Long runId, Instant now) {
        double value = numericValue(SensorType.VIBRATION, 0.0, 5.0, 8.0);
        return new SensorReadingMessage(SensorType.VIBRATION, "VIBRATION-1", null, MACHINE_ID,
                null, runId, value, "mm/s", null, null, now);
    }

    // ----- Value generation -----

    /**
     * Generates a realistic value using the active thresholds for this sensor type so the
     * stream contains mostly-normal readings with occasional warning/fail-range values.
     * Falls back to the provided default bands if no threshold is configured.
     */
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

    private double uniform(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
