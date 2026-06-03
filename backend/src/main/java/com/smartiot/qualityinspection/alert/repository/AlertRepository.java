package com.smartiot.qualityinspection.alert.repository;

import com.smartiot.qualityinspection.alert.model.Alert;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<Alert> findTop100ByOrderByCreatedAtDesc();

    long countByStatus(AlertStatus status);
}
