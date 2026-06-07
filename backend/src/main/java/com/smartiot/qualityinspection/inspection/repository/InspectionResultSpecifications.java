package com.smartiot.qualityinspection.inspection.repository;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.simulation.model.SimulationRun;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications for the inspection-result filtered search (FR-14, FR-21).
 *
 * <p>Built dynamically so that an unset (null) filter contributes no predicate and therefore
 * no SQL bind parameter at all. This deliberately avoids the static
 * {@code (:param IS NULL OR ...)} JPQL pattern, which on PostgreSQL produces
 * "could not determine data type of parameter" / "function lower(bytea) does not exist"
 * errors when null parameters are sent without an inferable type.
 */
public final class InspectionResultSpecifications {

    private InspectionResultSpecifications() {
    }

    public static Specification<InspectionResult> filter(QualityStatus status, Long batchId, Long simulationRunId,
                                                         String runName, Instant from, Instant to,
                                                         SensorType sensorType) {
        String trimmedRunName = (runName == null || runName.isBlank()) ? null : runName.trim();

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (batchId != null) {
                predicates.add(cb.equal(root.get("batchId"), batchId));
            }
            if (simulationRunId != null) {
                predicates.add(cb.equal(root.get("simulationRunId"), simulationRunId));
            }
            if (trimmedRunName != null) {
                Subquery<Long> runSub = query.subquery(Long.class);
                Root<SimulationRun> runRoot = runSub.from(SimulationRun.class);
                runSub.select(runRoot.get("id"))
                        .where(cb.like(cb.lower(runRoot.get("name")),
                                "%" + trimmedRunName.toLowerCase() + "%"));
                predicates.add(root.get("simulationRunId").in(runSub));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (sensorType != null) {
                Subquery<Long> readingSub = query.subquery(Long.class);
                Root<SensorReading> readingRoot = readingSub.from(SensorReading.class);
                readingSub.select(cb.literal(1L))
                        .where(cb.equal(readingRoot.get("productCode"), root.get("productCode")),
                                cb.equal(readingRoot.get("sensorType"), sensorType));
                predicates.add(cb.exists(readingSub));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
