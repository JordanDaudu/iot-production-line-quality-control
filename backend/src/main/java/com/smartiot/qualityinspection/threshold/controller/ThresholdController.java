package com.smartiot.qualityinspection.threshold.controller;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.threshold.dto.ThresholdDto;
import com.smartiot.qualityinspection.threshold.dto.ThresholdUpdateRequest;
import com.smartiot.qualityinspection.threshold.service.ThresholdService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Threshold configuration. Reading is open to authenticated users; editing requires the
 * Administrator role (enforced server-side, FR-18 / NFR-11).
 */
@RestController
@RequestMapping("/api/thresholds")
public class ThresholdController {

    private final ThresholdService thresholdService;

    public ThresholdController(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

    @GetMapping
    public List<ThresholdDto> list() {
        return thresholdService.list();
    }

    @PutMapping("/{sensorType}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ThresholdDto update(@PathVariable SensorType sensorType,
                               @Valid @RequestBody ThresholdUpdateRequest request,
                               Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(Object::toString)
                .findFirst()
                .map(a -> a.replace("ROLE_", ""))
                .orElse("UNKNOWN");
        return thresholdService.update(sensorType, request, role);
    }
}
