package com.smartiot.qualityinspection.simulation.controller;

import com.smartiot.qualityinspection.simulation.dto.FaultInjectionRequest;
import com.smartiot.qualityinspection.simulation.dto.FaultInjectionResponse;
import com.smartiot.qualityinspection.simulation.dto.SimulationStatusDto;
import com.smartiot.qualityinspection.simulation.dto.StartSimulationRequest;
import com.smartiot.qualityinspection.simulation.service.FaultInjectionService;
import com.smartiot.qualityinspection.simulation.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulation control endpoints. Reading the state is open to any authenticated user;
 * controlling the simulation requires an Operator or Administrator role (enforced
 * server-side per NFR-11). Invalid state transitions return HTTP 400.
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;
    private final FaultInjectionService faultInjectionService;

    public SimulationController(SimulationService simulationService,
                                FaultInjectionService faultInjectionService) {
        this.simulationService = simulationService;
        this.faultInjectionService = faultInjectionService;
    }

    @GetMapping("/state")
    public SimulationStatusDto state() {
        return simulationService.getStatus();
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMINISTRATOR')")
    public SimulationStatusDto start(@RequestBody(required = false) StartSimulationRequest request) {
        String scenario = request != null ? request.scenario() : null;
        return simulationService.start(scenario);
    }

    @PostMapping("/pause")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMINISTRATOR')")
    public SimulationStatusDto pause() {
        return simulationService.pause();
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMINISTRATOR')")
    public SimulationStatusDto stop() {
        return simulationService.stop();
    }

    @PostMapping("/reset")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMINISTRATOR')")
    public SimulationStatusDto reset() {
        return simulationService.reset();
    }

    @PostMapping("/faults")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public FaultInjectionResponse injectFault(@Valid @RequestBody FaultInjectionRequest request) {
        String message = faultInjectionService.inject(
                request.faultType(), request.sensorKey(), request.durationSeconds());
        return new FaultInjectionResponse(request.faultType().name(), message);
    }
}

