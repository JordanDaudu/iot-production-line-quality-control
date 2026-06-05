package com.smartiot.qualityinspection.simulation.model;

import com.smartiot.qualityinspection.common.enums.SimulationState;
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
 * A single controlled execution of the production-line simulation. Owns the scenario,
 * current state and the batches/products generated during the run.
 */
@Entity
@Table(name = "simulation_run")
public class SimulationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User-supplied name for this run. Unique (case-insensitive) across all runs; the
     * uniqueness check lives in the service layer. Nullable so runs created before this
     * field existed remain valid.
     */
    @Column(unique = true)
    private String name;

    /** Scenario name, e.g. NORMAL_RUN, HIGH_DEFECT_RATE. */
    @Column(nullable = false)
    private String scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SimulationState state;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant stoppedAt;

    protected SimulationRun() {
        // Required by JPA.
    }

    public SimulationRun(String name, String scenario, SimulationState state, Instant startedAt) {
        this.name = name;
        this.scenario = scenario;
        this.state = state;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public SimulationState getState() {
        return state;
    }

    public void setState(SimulationState state) {
        this.state = state;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(Instant stoppedAt) {
        this.stoppedAt = stoppedAt;
    }
}
