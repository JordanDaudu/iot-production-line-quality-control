package com.smartiot.qualityinspection.simulation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A grouping of products produced within a simulation run, used for production-run analysis.
 */
@Entity
@Table(name = "batch")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable code, e.g. BATCH001. */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Long simulationRunId;

    @Column(nullable = false)
    private Instant createdAt;

    protected Batch() {
        // Required by JPA.
    }

    public Batch(String code, Long simulationRunId, Instant createdAt) {
        this.code = code;
        this.simulationRunId = simulationRunId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
