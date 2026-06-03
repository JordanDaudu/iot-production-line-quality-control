package com.smartiot.qualityinspection.product.controller;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.product.dto.ProductDetailDto;
import com.smartiot.qualityinspection.product.service.ProductQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Product lookup, filtering and traceability. All endpoints are read-only and available to
 * any authenticated user. Date filters accept ISO-8601 instants (e.g. 2026-06-03T00:00:00Z).
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductQueryService productQueryService;

    public ProductController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @GetMapping
    public List<InspectionResultDto> search(
            @RequestParam(required = false) QualityStatus status,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long simulationRunId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) SensorType sensorType) {
        return productQueryService.search(status, batchId, simulationRunId,
                parseInstant(from), parseInstant(to), sensorType);
    }

    @GetMapping("/{productCode}")
    public ProductDetailDto detail(@PathVariable String productCode) {
        return productQueryService.getProductDetail(productCode);
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Invalid date/time '" + value + "'. Use ISO-8601, e.g. 2026-06-03T00:00:00Z.");
        }
    }
}
