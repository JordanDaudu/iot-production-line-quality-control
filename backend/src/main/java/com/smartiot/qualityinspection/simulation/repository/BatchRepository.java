package com.smartiot.qualityinspection.simulation.repository;

import com.smartiot.qualityinspection.simulation.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByCode(String code);
}
