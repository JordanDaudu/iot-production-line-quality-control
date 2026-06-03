package com.smartiot.qualityinspection.product.service;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.alert.repository.AlertRepository;
import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.exception.ResourceNotFoundException;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.inspection.repository.InspectionResultRepository;
import com.smartiot.qualityinspection.product.dto.ProductDetailDto;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingDto;
import com.smartiot.qualityinspection.sensor.repository.SensorReadingRepository;
import com.smartiot.qualityinspection.simulation.model.Batch;
import com.smartiot.qualityinspection.simulation.model.Product;
import com.smartiot.qualityinspection.simulation.model.SimulationRun;
import com.smartiot.qualityinspection.simulation.repository.BatchRepository;
import com.smartiot.qualityinspection.simulation.repository.ProductRepository;
import com.smartiot.qualityinspection.simulation.repository.SimulationRunRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Read-only queries for product lookup, filtering and traceability drill-down. Reconstructs
 * the SimulationRun -> Batch -> Product -> Readings -> Result hierarchy from ID references.
 */
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final SimulationRunRepository simulationRunRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final InspectionResultRepository inspectionResultRepository;
    private final AlertRepository alertRepository;

    public ProductQueryService(ProductRepository productRepository,
                               BatchRepository batchRepository,
                               SimulationRunRepository simulationRunRepository,
                               SensorReadingRepository sensorReadingRepository,
                               InspectionResultRepository inspectionResultRepository,
                               AlertRepository alertRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.simulationRunRepository = simulationRunRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.inspectionResultRepository = inspectionResultRepository;
        this.alertRepository = alertRepository;
    }

    /** Filtered list of inspected products (FR-14). */
    public List<InspectionResultDto> search(QualityStatus status, Long batchId, Long simulationRunId,
                                            Instant from, Instant to, SensorType sensorType) {
        return inspectionResultRepository.search(status, batchId, simulationRunId, from, to, sensorType)
                .stream().map(InspectionResultDto::from).toList();
    }

    /** Full traceability for one product (FR-13, FR-22). */
    public ProductDetailDto getProductDetail(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id " + productCode));

        String batchCode = batchRepository.findById(product.getBatchId())
                .map(Batch::getCode).orElse(null);
        String scenario = simulationRunRepository.findById(product.getSimulationRunId())
                .map(SimulationRun::getScenario).orElse(null);

        List<SensorReadingDto> readings = sensorReadingRepository
                .findByProductCodeOrderByTimestampAsc(productCode)
                .stream().map(SensorReadingDto::from).toList();

        InspectionResultDto result = inspectionResultRepository.findByProductCode(productCode)
                .map(InspectionResultDto::from).orElse(null);

        List<AlertDto> alerts = alertRepository.findByProductCodeOrderByCreatedAtDesc(productCode)
                .stream().map(AlertDto::from).toList();

        return new ProductDetailDto(
                product.getProductCode(),
                product.getBatchId(),
                batchCode,
                product.getSimulationRunId(),
                scenario,
                product.getCreatedAt() != null ? product.getCreatedAt().toString() : null,
                result,
                readings,
                alerts
        );
    }
}
