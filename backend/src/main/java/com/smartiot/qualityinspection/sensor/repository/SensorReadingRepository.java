package com.smartiot.qualityinspection.sensor.repository;

import com.smartiot.qualityinspection.sensor.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    List<SensorReading> findByProductCodeOrderByTimestampAsc(String productCode);

    List<SensorReading> findTop50BySensorTypeOrderByTimestampDesc(
            com.smartiot.qualityinspection.common.enums.SensorType sensorType);

    List<SensorReading> findBySimulationRunIdAndSensorTypeOrderByTimestampAsc(
            Long simulationRunId, com.smartiot.qualityinspection.common.enums.SensorType sensorType);
}
