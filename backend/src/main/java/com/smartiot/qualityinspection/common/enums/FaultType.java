package com.smartiot.qualityinspection.common.enums;

/**
 * Predefined faults an administrator can inject to demonstrate edge cases and alert
 * behaviour (FR-20). All are simulated; no hardware is involved.
 */
public enum FaultType {
    /** Force the next product's weight above the maximum limit (expected FAIL). */
    OVERWEIGHT_PRODUCT,
    /** Force the next product's camera result to a critical CRACK defect (expected FAIL). */
    VISUAL_DEFECT,
    /** Force the next temperature reading above its limit (expected maintenance alert). */
    TEMPERATURE_SPIKE,
    /** Force the next vibration reading above its limit (expected maintenance alert). */
    VIBRATION_SPIKE,
    /** Stop a sensor from emitting for a while (expected offline + sensor-health alert). */
    SENSOR_DISCONNECT
}
