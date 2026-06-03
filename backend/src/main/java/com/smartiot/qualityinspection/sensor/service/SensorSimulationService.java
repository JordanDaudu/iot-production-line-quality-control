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
import java.util.List;
import java.util.Random;

/**
 * Virtual IoT sensor simulator. While a simulation is RUNNING, each scheduled tick runs
 * one production cycle: a new product (barcode), product-level readings (weight, camera)
 * and machine-level readings (temperature, vibration). Every reading is submitted through
 * {@link IngestionService} — the same validated inbound channel external clients use.
 *
 * <p>Numeric values evolve as a smooth mean-reverting random walk, so consecutive readings
 * stay close to each other and drift gradually (realistic machine behaviour) rather than
 * jumping across the band. Injected faults and fault scenarios still cause spikes, which
 * then recover smoothly.
 */
@Service
public class SensorSimulationService {

    private static final Logger log = LoggerFactory.getLogger(SensorSimulationService.class);

    private static final String MACHINE_ID = "STATION-1";
    private static final String[] DEFECTS = {"SCRATCH", "CRACK", "MISSING_LABEL", "UNKNOWN"};

    /** The fixed set of virtual sensors that emit heartbeats while running (FR-17). */
    private record SensorDef(String key, SensorType type) {
    }

    private static final List<SensorDef> SENSORS = List.of(
            new SensorDef("WEIGHT-1", SensorType.WEIGHT),
            new SensorDef("CAMERA-1", SensorType.CAMERA),
            new SensorDef("BARCODE-1", SensorType.BARCODE),
            new SensorDef("TEMPERATURE-1", SensorType.TEMPERATURE),
            new SensorDef("VIBRATION-1", SensorType.VIBRATION));

    private final SimulationService simulationService;
    private final IngestionService ingestionService;
    private final ProductRepository productRepository;
    private final ThresholdConfigurationRepository thresholdRepository;
    private final FaultInjectionService faultInjectionService;
    private final SensorHealthService sensorHealthService;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    // Smoothly-evolving current values for each numeric sensor (mid-band starting points).
    private double currentWeight = 100.0;
    private double currentTemperature = 24.0;
    private double currentVibration = 2.0;

    public SensorSimulationService(SimulationService simulationService,
                                   IngestionService ingestionService,
                                   ProductRepository productRepository,
                                   ThresholdConfigurationRepository thresholdRepository,
                                   FaultInjectionService faultInjectionService,
                                   SensorHealthService sensorHealthService,
                                   ApplicationEventPublisher eventPublisher) {
        this.simulationService = simulationService;
        this.ingestionService = ingestionService;
        this.productRepository = productRepository;
        this.thresholdRepository = thresholdRepository;
        this.faultInjectionService = faultInjectionService;
        this.sensorHealthService = sensorHealthService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Each enabled sensor sends a heartbeat on a fixed cadence (FR-17). Disconnected
     * sensors are skipped so the health monitor can detect them as offline.
     */
    @Scheduled(fixedDelayString = "${sensor.heartbeat-interval-ms:5000}")
    public void emitHeartbeats() {
        if (!simulationService.isRunning()) {
            return;
        }
        Instant now = Instant.now();
        for (SensorDef sensor : SENSORS) {
            if (!faultInjectionService.isDisconnected(sensor.key())) {
                sensorHealthService.heartbeat(sensor.key(), sensor.type(), now);
            }
        }
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
        if (forceFail) {
            currentWeight = failValue(SensorType.WEIGHT, 110.0);
        } else {
            // A higher fault rate gently pushes the process target toward the upper limit.
            double targetFraction = Math.min(1.0, 0.5 + scenario.weightFailRate * 1.5);
            currentWeight = walk(SensorType.WEIGHT, currentWeight, targetFraction, 0.16);
        }
        return new SensorReadingMessage(SensorType.WEIGHT, "WEIGHT-1", productCode, null,
                batchId, runId, currentWeight, "g", null, null, now);
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
        if (forceSpike) {
            currentTemperature = failValue(SensorType.TEMPERATURE, 35.0);
        } else {
            currentTemperature = walk(SensorType.TEMPERATURE, currentTemperature, 0.45, 0.14);
        }
        return new SensorReadingMessage(SensorType.TEMPERATURE, "TEMPERATURE-1", null, MACHINE_ID,
                null, runId, currentTemperature, "C", null, null, now);
    }

    private SensorReadingMessage vibrationReading(Long runId, Instant now, boolean forceSpike, SimulationScenario scenario) {
        if (forceSpike) {
            currentVibration = failValue(SensorType.VIBRATION, 8.0);
        } else if (scenario.vibrationSpikeChance > 0) {
            // Vibration-fault scenario: drift sustained above the warning level.
            currentVibration = walk(SensorType.VIBRATION, currentVibration, 1.35, 0.14);
        } else {
            currentVibration = walk(SensorType.VIBRATION, currentVibration, 0.4, 0.16);
        }
        return new SensorReadingMessage(SensorType.VIBRATION, "VIBRATION-1", null, MACHINE_ID,
                null, runId, currentVibration, "mm/s", null, null, now);
    }

    // ----- Value generation -----

    /**
     * One step of a mean-reverting random walk toward a target inside the sensor's band.
     * The value moves only a little each tick (gentle reversion + small noise), so the
     * signal looks smooth and realistic. {@code targetFraction} positions the target
     * relative to the PASS band (0.5 = middle, &gt;1 = above the warning limit);
     * {@code smoothness} scales the per-step noise.
     */
    private double walk(SensorType type, double current, double targetFraction, double smoothness) {
        ThresholdConfiguration t = thresholdRepository.findBySensorType(type).orElse(null);
        double min = t != null ? t.getMinValue() : 0.0;
        double warnMin = t != null ? t.getWarnMinValue() : 0.0;
        double warnMax = t != null ? t.getWarnMaxValue() : 1.0;
        double max = t != null ? t.getMaxValue() : warnMax;

        double band = Math.max(1.0, warnMax - warnMin);
        double target = warnMin + band * targetFraction;
        double next = current + 0.12 * (target - current) + band * smoothness * random.nextGaussian();

        double lower = min;
        double upper = max + (max - warnMax) * 1.2; // allow occasional excursions into the fail band
        return round1(Math.max(lower, Math.min(upper, next)));
    }

    /** A value above the hard limit, used for injected faults and spikes. */
    private double failValue(SensorType type, double defMax) {
        double max = thresholdRepository.findBySensorType(type)
                .map(ThresholdConfiguration::getMaxValue).orElse(defMax);
        return round1(max + uniform(3.0, 8.0));
    }

    private double uniform(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
