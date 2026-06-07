package com.smartiot.qualityinspection.inspection.repository;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {

    Optional<InspectionResult> findByProductCode(String productCode);

    long countByStatus(QualityStatus status);

    List<InspectionResult> findTop20ByOrderByCreatedAtDesc();

    /**
     * Filtered search for the products list and reports. Any null parameter is ignored,
     * so callers can combine filters freely (FR-14, FR-21).
     */
    @Query("""
            SELECT r FROM InspectionResult r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:batchId IS NULL OR r.batchId = :batchId)
              AND (:simulationRunId IS NULL OR r.simulationRunId = :simulationRunId)
              AND (:runName IS NULL OR r.simulationRunId IN (
                    SELECT run.id FROM SimulationRun run
                    WHERE LOWER(run.name) LIKE LOWER(CONCAT('%', CAST(:runName AS string), '%'))))
              AND (:from IS NULL OR r.createdAt >= :from)
              AND (:to IS NULL OR r.createdAt <= :to)
              AND (:sensorType IS NULL OR EXISTS (
                    SELECT 1 FROM SensorReading sr
                    WHERE sr.productCode = r.productCode AND sr.sensorType = :sensorType))
            ORDER BY r.createdAt DESC
            """)
    List<InspectionResult> search(@Param("status") QualityStatus status,
                                  @Param("batchId") Long batchId,
                                  @Param("simulationRunId") Long simulationRunId,
                                  @Param("runName") String runName,
                                  @Param("from") Instant from,
                                  @Param("to") Instant to,
                                  @Param("sensorType") SensorType sensorType);
}
