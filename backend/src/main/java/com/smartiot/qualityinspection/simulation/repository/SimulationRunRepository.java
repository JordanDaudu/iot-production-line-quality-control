package com.smartiot.qualityinspection.simulation.repository;

import com.smartiot.qualityinspection.simulation.model.SimulationRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SimulationRunRepository extends JpaRepository<SimulationRun, Long> {

    Optional<SimulationRun> findFirstByOrderByStartedAtDesc();
}
