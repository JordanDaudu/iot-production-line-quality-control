package com.smartiot.qualityinspection.sensor.model;

import com.smartiot.qualityinspection.common.enums.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A single validated, time-stamped sensor measurement. Product-level readings carry a
 * productCode; machine-level readings carry a machineId. Camera readings may carry a
 * defect category and confidence instead of (or alongside) a numeric value.
 *
 * <p>Indexed by the fields used for history screens, product details and reports
 * (NFR-03).
 */
@Entity
@Table(name = "sensor_reading", indexes = {
        @Index(name = "idx_reading_timestamp", columnList = "timestamp"),
        @Index(name = "idx_reading_sensor_type", columnList = "sensorType"),
        @Index(name = "idx_reading_product_code", columnList = "productCode"),
        @Index(name = "idx_reading_batch_id", columnList = "batchId"),
        @Index(name = "idx_reading_simulation_run_id", columnList = "simulationRunId")
})
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType sensorType;

    @Column(nullable = false)
    private String sensorKey;

    /** Product-level readings only. */
    private String productCode;

    /** Machine-level readings only. */
    private String machineId;

    private Long batchId;

    private Long simulationRunId;

    /** Numeric value (weight, temperature, vibration). Null for pure camera results.
     *  Column is named "reading_value" because VALUE is a reserved SQL keyword. */
    @Column(name = "reading_value")
    private Double value;

    private String unit;

    /** Camera readings: OK, SCRATCH, CRACK, MISSING_LABEL, UNKNOWN. */
    private String defectCategory;

    /** Optional simulated AI confidence score 0-100 for camera readings. */
    private Double confidence;

    @Column(nullable = false)
    private Instant timestamp;

    public SensorReading() {
        // Public no-arg constructor: required by JPA and used by the ingestion mapper.
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

    public String getSensorKey() {
        return sensorKey;
    }

    public void setSensorKey(String sensorKey) {
        this.sensorKey = sensorKey;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getSimulationRunId() {
        return simulationRunId;
    }

    public void setSimulationRunId(Long simulationRunId) {
        this.simulationRunId = simulationRunId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDefectCategory() {
        return defectCategory;
    }

    public void setDefectCategory(String defectCategory) {
        this.defectCategory = defectCategory;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
