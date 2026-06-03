package com.smartiot.qualityinspection.inspection.service;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.inspection.service.QualityInspectionEngine.ClassificationOutcome;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure quality engine. Covers PASS/WARNING/FAIL classification and
 * explanation ordering (TC-E2E-01, TC-E2E-02, TC-E2E-11).
 */
class QualityInspectionEngineTest {

    private final QualityInspectionEngine engine = new QualityInspectionEngine();

    private ThresholdConfiguration weightThreshold() {
        // min 90, warn 95-105, max 110
        return new ThresholdConfiguration(SensorType.WEIGHT, 90.0, 95.0, 105.0, 110.0, "g", Instant.now());
    }

    private SensorReading weight(double value) {
        SensorReading r = new SensorReading();
        r.setSensorType(SensorType.WEIGHT);
        r.setSensorKey("WEIGHT-1");
        r.setValue(value);
        r.setUnit("g");
        r.setTimestamp(Instant.now());
        return r;
    }

    private SensorReading camera(String defect) {
        SensorReading r = new SensorReading();
        r.setSensorType(SensorType.CAMERA);
        r.setSensorKey("CAMERA-1");
        r.setDefectCategory(defect);
        r.setTimestamp(Instant.now());
        return r;
    }

    @Test
    void normalProductPasses() {
        ClassificationOutcome outcome = engine.classify(weight(100), camera("OK"), weightThreshold());
        assertEquals(QualityStatus.PASS, outcome.status());
    }

    @Test
    void nearLimitWeightWarns() {
        ClassificationOutcome outcome = engine.classify(weight(108), camera("OK"), weightThreshold());
        assertEquals(QualityStatus.WARNING, outcome.status());
    }

    @Test
    void overweightFails() {
        ClassificationOutcome outcome = engine.classify(weight(120), camera("OK"), weightThreshold());
        assertEquals(QualityStatus.FAIL, outcome.status());
    }

    @Test
    void criticalDefectFails() {
        ClassificationOutcome outcome = engine.classify(weight(100), camera("CRACK"), weightThreshold());
        assertEquals(QualityStatus.FAIL, outcome.status());
        assertTrue(outcome.explanation().startsWith("Critical visual defect"),
                "FAIL reason should come first: " + outcome.explanation());
    }

    @Test
    void minorDefectWarns() {
        ClassificationOutcome outcome = engine.classify(weight(100), camera("SCRATCH"), weightThreshold());
        assertEquals(QualityStatus.WARNING, outcome.status());
    }

    @Test
    void explanationListsMostSevereReasonFirst() {
        // FAIL weight + WARNING defect -> overall FAIL, weight reason listed first.
        ClassificationOutcome outcome = engine.classify(weight(120), camera("SCRATCH"), weightThreshold());
        assertEquals(QualityStatus.FAIL, outcome.status());
        int weightIdx = outcome.explanation().indexOf("outside");
        int defectIdx = outcome.explanation().indexOf("Visual defect");
        assertTrue(weightIdx >= 0 && defectIdx >= 0 && weightIdx < defectIdx,
                "Most severe (FAIL) reason should precede WARNING: " + outcome.explanation());
    }
}
