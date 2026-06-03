package com.smartiot.qualityinspection.common.enums;

/**
 * Final quality classification produced by the inspection engine for a product.
 */
public enum QualityStatus {
    /** Product meets all active inspection thresholds. */
    PASS,
    /** Product or machine behaviour is near limits and needs attention. */
    WARNING,
    /** Product failed one or more inspection rules. */
    FAIL
}
