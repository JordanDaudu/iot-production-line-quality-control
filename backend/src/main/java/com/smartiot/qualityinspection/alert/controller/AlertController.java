package com.smartiot.qualityinspection.alert.controller;

import com.smartiot.qualityinspection.alert.dto.AcknowledgeAlertRequest;
import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.alert.service.AlertService;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Alert listing and lifecycle. Listing is open to authenticated users; acknowledging and
 * resolving require a Maintenance Technician or Administrator role (FR-24 / NFR-11).
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertDto> list(@RequestParam(required = false) AlertStatus status) {
        return alertService.listAlerts(status);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'ADMINISTRATOR')")
    public AlertDto acknowledge(@PathVariable Long id,
                                @RequestBody(required = false) AcknowledgeAlertRequest request,
                                Authentication authentication) {
        String note = request != null ? request.note() : null;
        return alertService.acknowledge(id, authentication.getName(), note);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'ADMINISTRATOR')")
    public AlertDto resolve(@PathVariable Long id, Authentication authentication) {
        return alertService.resolve(id, authentication.getName());
    }
}
