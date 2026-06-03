package com.smartiot.qualityinspection.common.enums;

/**
 * The virtual IoT sensor types available on the simulated production line.
 *
 * <p>WEIGHT, CAMERA and BARCODE are product-level. TEMPERATURE, VIBRATION and HEALTH
 * are machine-level.
 */
public enum SensorType {
    WEIGHT,
    TEMPERATURE,
    VIBRATION,
    CAMERA,
    BARCODE,
    HEALTH
}
