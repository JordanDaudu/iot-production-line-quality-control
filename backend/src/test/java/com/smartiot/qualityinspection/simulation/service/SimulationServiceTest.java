package com.smartiot.qualityinspection.simulation.service;

import com.smartiot.qualityinspection.common.enums.SimulationState;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.simulation.model.Batch;
import com.smartiot.qualityinspection.simulation.model.SimulationRun;
import com.smartiot.qualityinspection.simulation.repository.BatchRepository;
import com.smartiot.qualityinspection.simulation.repository.SimulationRunRepository;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the simulation state machine, including invalid transitions (FR-19,
 * TC-E2E-06).
 */
@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private SimulationRunRepository runRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private RealtimeBroadcaster broadcaster;
    @Mock
    private FaultInjectionService faultInjectionService;

    @InjectMocks
    private SimulationService service;

    private void stubSaves() {
        when(runRepository.save(any())).thenAnswer(inv -> {
            SimulationRun run = inv.getArgument(0);
            ReflectionTestUtils.setField(run, "id", 1L);
            return run;
        });
        when(batchRepository.save(any())).thenAnswer(inv -> {
            Batch batch = inv.getArgument(0);
            ReflectionTestUtils.setField(batch, "id", 2L);
            return batch;
        });
    }

    @Test
    void startMovesToRunning() {
        stubSaves();
        var status = service.start("NORMAL_RUN");
        assertEquals(SimulationState.RUNNING, status.state());
        assertEquals(1L, status.simulationRunId());
        verify(broadcaster).broadcastSimulationState(status);
    }

    @Test
    void startWhileRunningIsRejected() {
        stubSaves();
        service.start("NORMAL_RUN");
        assertThrows(ValidationException.class, () -> service.start("NORMAL_RUN"));
    }

    @Test
    void pauseWhenIdleIsRejected() {
        assertThrows(ValidationException.class, () -> service.pause());
    }

    @Test
    void stopWhenIdleIsRejected() {
        assertThrows(ValidationException.class, () -> service.stop());
    }

    @Test
    void resetReturnsToIdleAndClearsFaults() {
        var status = service.reset();
        assertEquals(SimulationState.IDLE, status.state());
        verify(faultInjectionService).reset();
    }
}
