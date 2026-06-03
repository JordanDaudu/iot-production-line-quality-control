package com.smartiot.qualityinspection.inspection.repository;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {

    Optional<InspectionResult> findByProductCode(String productCode);

    long countByStatus(QualityStatus status);

    List<InspectionResult> findTop20ByOrderByCreatedAtDesc();
}
