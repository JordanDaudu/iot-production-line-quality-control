package com.smartiot.qualityinspection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduled task support, used by the virtual sensor simulators to emit
 * readings on a fixed cadence while a simulation is running.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
