package com.smartiot.qualityinspection.alert.dto;

/**
 * Optional body for acknowledging an alert, carrying a free-text note (FR-24).
 */
public record AcknowledgeAlertRequest(String note) {
}
