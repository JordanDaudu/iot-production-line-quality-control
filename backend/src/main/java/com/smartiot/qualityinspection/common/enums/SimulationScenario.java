package com.smartiot.qualityinspection.common.enums;

/**
 * Predefined simulation scenarios that change virtual sensor behaviour (FR-25). Each
 * scenario tunes how often defects, fail-range weights and machine spikes occur, and
 * whether sensors occasionally drop.
 */
public enum SimulationScenario {

    //                 defectRate, weightFailRate, tempSpikeChance, vibSpikeChance, disconnectChance
    NORMAL_RUN(0.15, 0.05, 0.00, 0.00, 0.00),
    HIGH_DEFECT_RATE(0.50, 0.30, 0.00, 0.00, 0.00),
    TEMPERATURE_SPIKE(0.15, 0.05, 0.30, 0.00, 0.00),
    VIBRATION_FAULT(0.15, 0.05, 0.00, 0.30, 0.00),
    SENSOR_DISCONNECT(0.15, 0.05, 0.00, 0.00, 0.08),
    MIXED_FAULT_DEMO(0.35, 0.20, 0.15, 0.15, 0.05);

    public final double defectRate;
    public final double weightFailRate;
    public final double tempSpikeChance;
    public final double vibrationSpikeChance;
    public final double disconnectChance;

    SimulationScenario(double defectRate, double weightFailRate,
                       double tempSpikeChance, double vibrationSpikeChance, double disconnectChance) {
        this.defectRate = defectRate;
        this.weightFailRate = weightFailRate;
        this.tempSpikeChance = tempSpikeChance;
        this.vibrationSpikeChance = vibrationSpikeChance;
        this.disconnectChance = disconnectChance;
    }

    /** Resolves a scenario name to a profile, falling back to NORMAL_RUN if unknown. */
    public static SimulationScenario fromName(String name) {
        if (name == null) {
            return NORMAL_RUN;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
            return NORMAL_RUN;
        }
    }
}
