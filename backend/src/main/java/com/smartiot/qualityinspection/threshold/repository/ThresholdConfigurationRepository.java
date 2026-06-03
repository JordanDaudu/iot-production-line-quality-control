package com.smartiot.qualityinspection.threshold.repository;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThresholdConfigurationRepository extends JpaRepository<ThresholdConfiguration, Long> {

    Optional<ThresholdConfiguration> findBySensorType(SensorType sensorType);
}
