package com.smartiot.qualityinspection.inspection.service;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Pure quality classification rules (no persistence, no I/O) so they are easy to test and
 * reason about. Combines the product's weight and visual (camera) readings into a single
 * PASS / WARNING / FAIL outcome with a human-readable explanation (FR-10, FR-11).
 *
 * <p>Weight bands come from the active {@link ThresholdConfiguration}. Camera defects map
 * to: OK = pass, CRACK = fail (critical), any other defect = warning. The overall status
 * is the most severe individual reason, and the explanation lists the most severe reason
 * first.
 */
@Service
public class QualityInspectionEngine {

    /** A single contributing reason behind the decision. */
    public record Reason(QualityStatus severity, String text) {
    }

    /** The final outcome returned to the inspection service. */
    public record ClassificationOutcome(QualityStatus status, Double score, String explanation) {
    }

    public ClassificationOutcome classify(SensorReading weight,
                                          SensorReading camera,
                                          ThresholdConfiguration weightThreshold) {
        List<Reason> reasons = new ArrayList<>();

        evaluateWeight(weight, weightThreshold, reasons);
        evaluateCamera(camera, reasons);

        QualityStatus status = worstSeverity(reasons);
        String explanation = buildExplanation(reasons, status);
        double score = computeScore(reasons);
        return new ClassificationOutcome(status, score, explanation);
    }

    private void evaluateWeight(SensorReading weight, ThresholdConfiguration t, List<Reason> reasons) {
        if (weight == null || weight.getValue() == null || t == null) {
            return;
        }
        double v = weight.getValue();
        String unit = weight.getUnit() != null ? " " + weight.getUnit() : "";
        if (v < t.getMinValue() || v > t.getMaxValue()) {
            reasons.add(new Reason(QualityStatus.FAIL, String.format(Locale.US,
                    "Weight %.1f%s is outside the acceptable range [%.1f, %.1f]",
                    v, unit, t.getMinValue(), t.getMaxValue())));
        } else if (v < t.getWarnMinValue() || v > t.getWarnMaxValue()) {
            reasons.add(new Reason(QualityStatus.WARNING, String.format(Locale.US,
                    "Weight %.1f%s is near the limit (ideal %.1f-%.1f)",
                    v, unit, t.getWarnMinValue(), t.getWarnMaxValue())));
        } else {
            reasons.add(new Reason(QualityStatus.PASS, String.format(Locale.US,
                    "Weight %.1f%s is within limits", v, unit)));
        }
    }

    private void evaluateCamera(SensorReading camera, List<Reason> reasons) {
        if (camera == null || camera.getDefectCategory() == null) {
            return;
        }
        String defect = camera.getDefectCategory();
        switch (defect) {
            case "OK" -> reasons.add(new Reason(QualityStatus.PASS, "No visual defects detected"));
            case "CRACK" -> reasons.add(new Reason(QualityStatus.FAIL, "Critical visual defect detected: CRACK"));
            default -> reasons.add(new Reason(QualityStatus.WARNING, "Visual defect detected: " + defect));
        }
    }

    private QualityStatus worstSeverity(List<Reason> reasons) {
        // QualityStatus is declared PASS, WARNING, FAIL, so a higher ordinal is more severe.
        return reasons.stream()
                .map(Reason::severity)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(QualityStatus.PASS);
    }

    private String buildExplanation(List<Reason> reasons, QualityStatus status) {
        if (reasons.isEmpty()) {
            return "No product-level readings were available to classify this product.";
        }
        if (status == QualityStatus.PASS) {
            return "All checks passed: " + joinTexts(reasons) + ".";
        }
        // Most severe reason first (FR-11).
        List<Reason> ordered = reasons.stream()
                .sorted(Comparator.comparingInt((Reason r) -> r.severity().ordinal()).reversed())
                .toList();
        return joinTexts(ordered) + ".";
    }

    private String joinTexts(List<Reason> reasons) {
        return reasons.stream().map(Reason::text).reduce((a, b) -> a + "; " + b).orElse("");
    }

    private double computeScore(List<Reason> reasons) {
        if (reasons.isEmpty()) {
            return 100.0;
        }
        double total = 0;
        for (Reason r : reasons) {
            total += switch (r.severity()) {
                case PASS -> 100;
                case WARNING -> 65;
                case FAIL -> 25;
            };
        }
        return Math.round((total / reasons.size()) * 10.0) / 10.0;
    }
}
