package com.smartiot.qualityinspection.simulation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A manufactured item tracked through the production line. Identified by a unique
 * product code such as BATCH001-P0001.
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private Long simulationRunId;

    @Column(nullable = false)
    private Instant createdAt;

    protected Product() {
        // Required by JPA.
    }

    public Product(String productCode, Long batchId, Long simulationRunId, Instant createdAt) {
        this.productCode = productCode;
        this.batchId = batchId;
        this.simulationRunId = simulationRunId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
