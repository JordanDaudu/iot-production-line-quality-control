package com.smartiot.qualityinspection.threshold.model;

import com.smartiot.qualityinspection.common.enums.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Configurable quality limits for a sensor type, used by the inspection engine.
 *
 * <p>Values define the bands:
 * <pre>
 *   value &lt; minValue              -> FAIL (too low)
 *   minValue..warnMinValue        -> WARNING (low band)
 *   warnMinValue..warnMaxValue    -> PASS
 *   warnMaxValue..maxValue        -> WARNING (high band)
 *   value &gt; maxValue              -> FAIL (too high)
 * </pre>
 */
@Entity
@Table(name = "threshold_configuration")
public class ThresholdConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private SensorType sensorType;

    @Column(nullable = false)
    private Double minValue;

    @Column(nullable = false)
    private Double warnMinValue;

    @Column(nullable = false)
    private Double warnMaxValue;

    @Column(nullable = false)
    private Double maxValue;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Instant updatedAt;

    private String updatedByRole;

    protected ThresholdConfiguration() {
        // Required by JPA.
    }

    public ThresholdConfiguration(SensorType sensorType, Double minValue, Double warnMinValue,
                                  Double warnMaxValue, Double maxValue, String unit, Instant updatedAt) {
        this.sensorType = sensorType;
        this.minValue = minValue;
        this.warnMinValue = warnMinValue;
        this.warnMaxValue = warnMaxValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getWarnMinValue() {
        return warnMinValue;
    }

    public void setWarnMinValue(Double warnMinValue) {
        this.warnMinValue = warnMinValue;
    }

    public Double getWarnMaxValue() {
        return warnMaxValue;
    }

    public void setWarnMaxValue(Double warnMaxValue) {
        this.warnMaxValue = warnMaxValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }
}
