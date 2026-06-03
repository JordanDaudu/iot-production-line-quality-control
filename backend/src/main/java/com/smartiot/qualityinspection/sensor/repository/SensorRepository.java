package com.smartiot.qualityinspection.sensor.repository;

import com.smartiot.qualityinspection.sensor.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findBySensorKey(String sensorKey);
}
