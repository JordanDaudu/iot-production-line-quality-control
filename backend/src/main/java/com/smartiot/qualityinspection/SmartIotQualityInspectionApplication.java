package com.smartiot.qualityinspection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Smart IoT Quality Inspection System.
 *
 * <p>The application simulates a smart manufacturing production line: virtual sensors
 * generate readings, the backend validates and stores them, a quality engine classifies
 * each product (PASS / WARNING / FAIL), and the dashboard is updated live over STOMP.
 */
@SpringBootApplication
public class SmartIotQualityInspectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartIotQualityInspectionApplication.class, args);
    }
}
