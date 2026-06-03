package com.smartiot.qualityinspection.simulation.dto;

/**
 * Confirmation returned after a fault is queued for injection.
 */
public record FaultInjectionResponse(String faultType, String message) {
}
