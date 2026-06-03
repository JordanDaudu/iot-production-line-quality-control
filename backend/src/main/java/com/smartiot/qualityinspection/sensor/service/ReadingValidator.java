package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingMessage;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Validates inbound sensor messages before they are stored or classified (FR-08, NFR-04).
 * Rejecting bad data here protects the rest of the pipeline. Rules:
 *
 * <ul>
 *   <li>Required: sensorType, sensorKey, timestamp.</li>
 *   <li>Product-level sensors (WEIGHT, CAMERA, BARCODE) require a productCode.</li>
 *   <li>Machine-level sensors (TEMPERATURE, VIBRATION, HEALTH) require a machineId.</li>
 *   <li>Numeric sensors (WEIGHT, TEMPERATURE, VIBRATION) require a finite value.</li>
 *   <li>CAMERA requires a defectCategory.</li>
 * </ul>
 */
@Component
public class ReadingValidator {

    private static final Set<SensorType> PRODUCT_LEVEL =
            EnumSet.of(SensorType.WEIGHT, SensorType.CAMERA, SensorType.BARCODE);
    private static final Set<SensorType> MACHINE_LEVEL =
            EnumSet.of(SensorType.TEMPERATURE, SensorType.VIBRATION, SensorType.HEALTH);
    private static final Set<SensorType> NUMERIC =
            EnumSet.of(SensorType.WEIGHT, SensorType.TEMPERATURE, SensorType.VIBRATION);

    public void validate(SensorReadingMessage message) {
        if (message == null) {
            throw new ValidationException("Sensor message is missing.");
        }
        if (message.sensorType() == null) {
            throw new ValidationException("sensorType is required.");
        }
        if (isBlank(message.sensorKey())) {
            throw new ValidationException("sensorKey is required.");
        }
        if (message.timestamp() == null) {
            throw new ValidationException("timestamp is required.");
        }

        SensorType type = message.sensorType();

        if (PRODUCT_LEVEL.contains(type) && isBlank(message.productCode())) {
            throw new ValidationException("productCode is required for sensor type " + type + ".");
        }
        if (MACHINE_LEVEL.contains(type) && isBlank(message.machineId())) {
            throw new ValidationException("machineId is required for sensor type " + type + ".");
        }
        if (NUMERIC.contains(type)) {
            Double value = message.value();
            if (value == null || value.isNaN() || value.isInfinite()) {
                throw new ValidationException("A finite numeric value is required for sensor type " + type + ".");
            }
        }
        if (type == SensorType.CAMERA && isBlank(message.defectCategory())) {
            throw new ValidationException("defectCategory is required for CAMERA readings.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
