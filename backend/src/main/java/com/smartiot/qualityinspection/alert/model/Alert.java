package com.smartiot.qualityinspection.alert.model;

import com.smartiot.qualityinspection.common.enums.AlertSeverity;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import com.smartiot.qualityinspection.common.enums.AlertType;
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
 * A quality, maintenance or sensor-health notification. Supports an acknowledgement
 * lifecycle (ACTIVE -> ACKNOWLEDGED -> RESOLVED) that is preserved for history (FR-24).
 */
@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_type", columnList = "type"),
        @Index(name = "idx_alert_created_at", columnList = "createdAt")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    /** Free-text source context, e.g. sensor key or machine/station. */
    private String source;

    private String productCode;

    private String sensorKey;

    private Long simulationRunId;

    @Column(nullable = false)
    private Instant createdAt;

    private String acknowledgedBy;

    private Instant acknowledgedAt;

    private Instant resolvedAt;

    public Alert() {
        // Public no-arg constructor: required by JPA and used by AlertService.
    }

    public Long getId() {
        return id;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSensorKey() {
        return sensorKey;
    }

    public void setSensorKey(String sensorKey) {
        this.sensorKey = sensorKey;
    }

    public Long getSimulationRunId() {
        return simulationRunId;
    }

    public void setSimulationRunId(Long simulationRunId) {
        this.simulationRunId = simulationRunId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
