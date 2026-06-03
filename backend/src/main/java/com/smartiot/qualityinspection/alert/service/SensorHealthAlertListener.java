package com.smartiot.qualityinspection.alert.service;

import com.smartiot.qualityinspection.common.event.SensorRecoveredEvent;
import com.smartiot.qualityinspection.common.event.SensorWentOfflineEvent;
import com.smartiot.qualityinspection.dashboard.service.DashboardService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Bridges sensor-health transitions to alerts and dashboard refreshes. Lives in the alert
 * module so {@code SensorHealthService} stays free of any dependency on alerts/dashboard.
 */
@Component
public class SensorHealthAlertListener {

    private final AlertService alertService;
    private final DashboardService dashboardService;

    public SensorHealthAlertListener(AlertService alertService, DashboardService dashboardService) {
        this.alertService = alertService;
        this.dashboardService = dashboardService;
    }

    @EventListener
    public void onSensorOffline(SensorWentOfflineEvent event) {
        // createSensorHealthAlert already refreshes the dashboard summary.
        alertService.createSensorHealthAlert(event.sensor());
    }

    @EventListener
    public void onSensorRecovered(SensorRecoveredEvent event) {
        dashboardService.broadcastSummary();
    }
}
