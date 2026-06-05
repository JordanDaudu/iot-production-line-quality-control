package com.smartiot.qualityinspection.simulation.service;

import com.smartiot.qualityinspection.common.enums.SimulationState;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.simulation.dto.SimulationRunSummaryDto;
import com.smartiot.qualityinspection.simulation.dto.SimulationStatusDto;
import com.smartiot.qualityinspection.simulation.model.Batch;
import com.smartiot.qualityinspection.simulation.model.SimulationRun;
import com.smartiot.qualityinspection.simulation.repository.BatchRepository;
import com.smartiot.qualityinspection.simulation.repository.SimulationRunRepository;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns the production-line simulation lifecycle and the current run context. State is held
 * in memory (for the live tick loop) and mirrored to the SimulationRun record. All
 * mutating methods are synchronized so state transitions are atomic, and each broadcasts
 * the new state to dashboard clients.
 *
 * <p>Valid transitions:
 * <pre>
 *   start : IDLE/STOPPED -> RUNNING (new run)   |   PAUSED -> RUNNING (resume)
 *   pause : RUNNING -> PAUSED
 *   stop  : RUNNING/PAUSED -> STOPPED
 *   reset : any -> IDLE (clears live context; persisted history is kept)
 * </pre>
 */
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);
    private static final String DEFAULT_SCENARIO = "NORMAL_RUN";

    private final SimulationRunRepository runRepository;
    private final BatchRepository batchRepository;
    private final RealtimeBroadcaster broadcaster;
    private final FaultInjectionService faultInjectionService;

    // In-memory live context for the running simulation.
    private volatile SimulationState state = SimulationState.IDLE;
    private volatile Long currentRunId;
    private volatile String currentRunName;
    private volatile Long currentBatchId;
    private volatile String currentBatchCode;
    private volatile String scenario;
    private final AtomicInteger productSequence = new AtomicInteger(0);

    public SimulationService(SimulationRunRepository runRepository,
                             BatchRepository batchRepository,
                             RealtimeBroadcaster broadcaster,
                             FaultInjectionService faultInjectionService) {
        this.runRepository = runRepository;
        this.batchRepository = batchRepository;
        this.broadcaster = broadcaster;
        this.faultInjectionService = faultInjectionService;
    }

    public synchronized SimulationStatusDto start(String requestedName, String requestedScenario) {
        if (state == SimulationState.RUNNING) {
            throw new ValidationException("Simulation is already running.");
        }

        if (state == SimulationState.PAUSED) {
            // Resume the existing run. The name is fixed at creation, so it is ignored here.
            state = SimulationState.RUNNING;
            updateRunState(currentRunId, SimulationState.RUNNING, null);
            log.info("Simulation resumed (run {})", currentRunId);
            return broadcastStatus();
        }

        // Start a brand new run (from IDLE or STOPPED): a unique name is required.
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.isEmpty()) {
            throw new ValidationException("A run name is required.");
        }
        if (runRepository.existsByNameIgnoreCase(name)) {
            throw new ValidationException("A simulation named '" + name + "' already exists. Choose a different name.");
        }

        String scenarioName = (requestedScenario == null || requestedScenario.isBlank())
                ? DEFAULT_SCENARIO : requestedScenario;
        Instant now = Instant.now();

        SimulationRun run = runRepository.save(new SimulationRun(name, scenarioName, SimulationState.RUNNING, now));
        String batchCode = String.format("BATCH%03d", run.getId());
        Batch batch = batchRepository.save(new Batch(batchCode, run.getId(), now));

        this.currentRunId = run.getId();
        this.currentRunName = name;
        this.currentBatchId = batch.getId();
        this.currentBatchCode = batchCode;
        this.scenario = scenarioName;
        this.productSequence.set(0);
        this.state = SimulationState.RUNNING;

        log.info("Simulation started (run {} '{}', batch {}, scenario {})", currentRunId, name, batchCode, scenarioName);
        return broadcastStatus();
    }

    public synchronized SimulationStatusDto pause() {
        if (state != SimulationState.RUNNING) {
            throw new ValidationException("Only a running simulation can be paused.");
        }
        state = SimulationState.PAUSED;
        updateRunState(currentRunId, SimulationState.PAUSED, null);
        log.info("Simulation paused (run {})", currentRunId);
        return broadcastStatus();
    }

    public synchronized SimulationStatusDto stop() {
        if (state != SimulationState.RUNNING && state != SimulationState.PAUSED) {
            throw new ValidationException("Only a running or paused simulation can be stopped.");
        }
        state = SimulationState.STOPPED;
        updateRunState(currentRunId, SimulationState.STOPPED, Instant.now());
        log.info("Simulation stopped (run {})", currentRunId);
        return broadcastStatus();
    }

    public synchronized SimulationStatusDto reset() {
        // Reset clears the live runtime context only; persisted history remains.
        state = SimulationState.IDLE;
        currentRunId = null;
        currentRunName = null;
        currentBatchId = null;
        currentBatchCode = null;
        scenario = null;
        productSequence.set(0);
        faultInjectionService.reset();
        log.info("Simulation reset to IDLE; persisted history retained");
        return broadcastStatus();
    }

    public SimulationStatusDto getStatus() {
        return new SimulationStatusDto(currentRunId, currentRunName, scenario, state);
    }

    /** All persisted runs, most recent first. Used by the frontend to pre-check run names. */
    public List<SimulationRunSummaryDto> listRuns() {
        return runRepository.findAllByOrderByStartedAtDesc().stream()
                .map(run -> new SimulationRunSummaryDto(
                        run.getId(),
                        run.getName(),
                        run.getScenario(),
                        run.getState(),
                        run.getStartedAt() == null ? null : run.getStartedAt().toString()))
                .toList();
    }

    // ----- Accessors used by the sensor simulators -----

    public boolean isRunning() {
        return state == SimulationState.RUNNING;
    }

    public Long getCurrentRunId() {
        return currentRunId;
    }

    public Long getCurrentBatchId() {
        return currentBatchId;
    }

    /** The active scenario name (e.g. NORMAL_RUN), or null when idle. */
    public String getScenario() {
        return scenario;
    }

    /** Returns the next unique product code for the active run, e.g. BATCH001-P0001. */
    public String nextProductCode() {
        return currentBatchCode + "-P" + String.format("%04d", productSequence.incrementAndGet());
    }

    // ----- Internals -----

    private void updateRunState(Long runId, SimulationState newState, Instant stoppedAt) {
        if (runId == null) {
            return;
        }
        runRepository.findById(runId).ifPresent(run -> {
            run.setState(newState);
            if (stoppedAt != null) {
                run.setStoppedAt(stoppedAt);
            }
            runRepository.save(run);
        });
    }

    private SimulationStatusDto broadcastStatus() {
        SimulationStatusDto status = getStatus();
        broadcaster.broadcastSimulationState(status);
        return status;
    }
}
