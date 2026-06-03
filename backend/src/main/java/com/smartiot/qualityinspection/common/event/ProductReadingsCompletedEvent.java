package com.smartiot.qualityinspection.common.event;

/**
 * Published by the simulator once all product-level readings for a product have been
 * submitted, signalling that the product is ready for quality classification. The
 * inspection module listens for this; the simulator does not depend on it directly.
 */
public record ProductReadingsCompletedEvent(
        String productCode,
        Long batchId,
        Long simulationRunId
) {
}
