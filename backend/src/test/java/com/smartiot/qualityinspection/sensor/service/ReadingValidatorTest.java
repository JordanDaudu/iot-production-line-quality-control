package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the ingestion validator. Covers acceptance of valid packets and rejection
 * of malformed/incomplete ones (FR-08, TC-E2E-09).
 */
class ReadingValidatorTest {

    private final ReadingValidator validator = new ReadingValidator();

    private SensorReadingMessage msg(SensorType type, String productCode, String machineId,
                                     Double value, String defect, Instant ts) {
        return new SensorReadingMessage(type, type != null ? type.name() + "-1" : "X", productCode,
                machineId, 1L, 1L, value, "g", defect, null, ts);
    }

    @Test
    void validProductWeightIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(
                msg(SensorType.WEIGHT, "BATCH001-P0001", null, 100.0, null, Instant.now())));
    }

    @Test
    void missingProductCodeIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(
                msg(SensorType.WEIGHT, null, null, 100.0, null, Instant.now())));
    }

    @Test
    void missingNumericValueIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(
                msg(SensorType.WEIGHT, "BATCH001-P0001", null, null, null, Instant.now())));
    }

    @Test
    void cameraWithoutDefectCategoryIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(
                msg(SensorType.CAMERA, "BATCH001-P0001", null, null, null, Instant.now())));
    }

    @Test
    void machineReadingWithoutMachineIdIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(
                msg(SensorType.TEMPERATURE, null, null, 25.0, null, Instant.now())));
    }

    @Test
    void nullSensorTypeIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(
                msg(null, "BATCH001-P0001", null, 100.0, null, Instant.now())));
    }

    @Test
    void missingTimestampIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(
                msg(SensorType.WEIGHT, "BATCH001-P0001", null, 100.0, null, null)));
    }
}
