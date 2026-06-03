package com.smartiot.qualityinspection.inspection.model;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
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
 * The final quality classification for a product. Exactly one result exists per product
 * (productCode is unique). Stores the human-readable explanation behind the decision so
 * it can be shown on the dashboard and product details view (FR-11).
 */
@Entity
@Table(name = "inspection_result", indexes = {
        @Index(name = "idx_result_status", columnList = "status"),
        @Index(name = "idx_result_batch_id", columnList = "batchId"),
        @Index(name = "idx_result_simulation_run_id", columnList = "simulationRunId"),
        @Index(name = "idx_result_created_at", columnList = "createdAt")
})
public class InspectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private Long simulationRunId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualityStatus status;

    /** Optional aggregate quality score 0-100. */
    private Double score;

    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(nullable = false)
    private Instant createdAt;

    protected InspectionResult() {
        // Required by JPA.
    }

    public InspectionResult(String productCode, Long batchId, Long simulationRunId,
                            QualityStatus status, Double score, String explanation, Instant createdAt) {
        this.productCode = productCode;
        this.batchId = batchId;
        this.simulationRunId = simulationRunId;
        this.status = status;
        this.score = score;
        this.explanation = explanation;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
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

    public QualityStatus getStatus() {
        return status;
    }

    public void setStatus(QualityStatus status) {
        this.status = status;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
