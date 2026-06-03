package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.enums.FaultType;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.enums.SimulationScenario;
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
 * {@link IngestionService} — the same validated inbound channel external clients use.
 *
 * <p>The active {@link SimulationScenario} tunes defect/fail rates and machine spikes, so
 * different scenarios produce visibly different behaviour (FR-25). Injected faults are
 * applied on top of the scenario; disconnected sensors simply stop emitting so the health
 * monitor detects them as offline.
 */
@Service
public class SensorSimulationService {

    private static final Logger log = LoggerFactory.getLogger(SensorSimulationService.class);

    private static final String MACHINE_ID = "STATION-1";
    private static final String[] DEFECTS = {"SCRATCH", "CRACK", "MISSING_LABEL", "UNKNOWN"};

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

        SimulationScenario scenario = SimulationScenario.fromName(simulationService.getScenario());

        // Scenario-driven sensor disconnects.
        if (random.nextDouble() < scenario.disconnectChance && !faultInjectionService.isDisconnected("VIBRATION-1")) {
            faultInjectionService.inject(FaultType.SENSOR_DISCONNECT, "VIBRATION-1", 12);
        }

        // Faults (one-shot) combine with the scenario probabilities.
        boolean overweight = faultInjectionService.consumeOverweight();
        boolean visualDefect = faultInjectionService.consumeVisualDefect();
        boolean tempSpike = faultInjectionService.consumeTemperatureSpike() || random.nextDouble() < scenario.tempSpikeChance;
        boolean vibrationSpike = faultInjectionService.consumeVibrationSpike();

        Instant now = Instant.now();
        String productCode = simulationService.nextProductCode();
        if (productRepository.existsByProductCode(productCode)) {
            // Defensive: never reuse a product id within a run (FR-06).
            log.warn("Duplicate product code {} skipped", productCode);
            return;
        }
        productRepository.save(new Product(productCode, batchId, runId, now));

        emitIfConnected(barcodeReading(productCode, batchId, runId, now));
        emitIfConnected(weightReading(productCode, batchId, runId, now, overweight, scenario));
        emitIfConnected(cameraReading(productCode, batchId, runId, now, visualDefect, scenario));
        emitIfConnected(temperatureReading(runId, now, tempSpike));
        emitIfConnected(vibrationReading(runId, now, vibrationSpike, scenario));

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

    private SensorReadingMessage weightReading(String productCode, Long batchId, Long runId, Instant now,
                                               boolean forceFail, SimulationScenario scenario) {
        boolean fail = forceFail || random.nextDouble() < scenario.weightFailRate;
        double value = fail ? failValue(SensorType.WEIGHT, 110.0) : numericValue(SensorType.WEIGHT, 95.0, 105.0, 110.0);
        return new SensorReadingMessage(SensorType.WEIGHT, "WEIGHT-1", productCode, null,
                batchId, runId, value, "g", null, null, now);
    }

    private SensorReadingMessage cameraReading(String productCode, Long batchId, Long runId, Instant now,
                                               boolean forceDefect, SimulationScenario scenario) {
        String defect;
        if (forceDefect) {
            defect = "CRACK";
        } else if (random.nextDouble() < scenario.defectRate) {
            defect = DEFECTS[random.nextInt(DEFECTS.length)];
        } else {
            defect = "OK";
        }
        double confidence = "OK".equals(defect) ? uniform(85, 100) : uniform(45, 90);
        return new SensorReadingMessage(SensorType.CAMERA, "CAMERA-1", productCode, null,
                batchId, runId, null, null, defect, round1(confidence), now);
    }

    private SensorReadingMessage temperatureReading(Long runId, Instant now, boolean forceSpike) {
        double value = forceSpike ? failValue(SensorType.TEMPERATURE, 35.0) : numericValue(SensorType.TEMPERATURE, 18.0, 30.0, 35.0);
        return new SensorReadingMessage(SensorType.TEMPERATURE, "TEMPERATURE-1", null, MACHINE_ID,
                null, runId, value, "C", null, null, now);
    }

    private SensorReadingMessage vibrationReading(Long runId, Instant now, boolean forceSpike, SimulationScenario scenario) {
        double value;
        if (forceSpike) {
            value = failValue(SensorType.VIBRATION, 8.0);
        } else if (scenario.vibrationSpikeChance > 0) {
            // Vibration-fault scenario: readings stay consistently elevated (sustained).
            value = random.nextDouble() < 0.5
                    ? failValue(SensorType.VIBRATION, 8.0)
                    : elevatedValue(SensorType.VIBRATION, 5.0, 8.0);
        } else {
            value = numericValue(SensorType.VIBRATION, 0.0, 5.0, 8.0);
        }
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
        if (roll < 0.85) {
            value = uniform(warnMin, warnMax);                                  // PASS band
        } else {
            value = uniform(warnMax, max);                                      // WARNING band
        }
        return round1(value);
    }

    /** A value in the warning band (between warnMax and max), used for sustained elevation. */
    private double elevatedValue(SensorType type, double defWarnMax, double defMax) {
        ThresholdConfiguration t = thresholdRepository.findBySensorType(type).orElse(null);
        double warnMax = t != null ? t.getWarnMaxValue() : defWarnMax;
        double max = t != null ? t.getMaxValue() : defMax;
        return round1(uniform(warnMax, max));
    }

    /** A value above the hard limit, used for fail-band weights and injected spikes. */
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
