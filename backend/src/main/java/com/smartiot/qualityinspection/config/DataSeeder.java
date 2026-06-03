package com.smartiot.qualityinspection.config;

import com.smartiot.qualityinspection.auth.model.UserAccount;
import com.smartiot.qualityinspection.auth.repository.UserAccountRepository;
import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.enums.UserRole;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Seeds demo data on startup: one user per role and default numeric thresholds for the
 * weight/temperature/vibration sensors. Seeding is idempotent — it only runs when the
 * relevant tables are empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserAccountRepository userAccountRepository;
    private final ThresholdConfigurationRepository thresholdRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserAccountRepository userAccountRepository,
                      ThresholdConfigurationRepository thresholdRepository,
                      PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.thresholdRepository = thresholdRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedThresholds();
    }

    private void seedUsers() {
        if (userAccountRepository.count() > 0) {
            return;
        }
        userAccountRepository.save(new UserAccount(
                "manager", passwordEncoder.encode("manager123"), UserRole.QUALITY_MANAGER, "Quality Manager"));
        userAccountRepository.save(new UserAccount(
                "operator", passwordEncoder.encode("operator123"), UserRole.OPERATOR, "Production Line Operator"));
        userAccountRepository.save(new UserAccount(
                "tech", passwordEncoder.encode("tech123"), UserRole.MAINTENANCE_TECHNICIAN, "Maintenance Technician"));
        userAccountRepository.save(new UserAccount(
                "admin", passwordEncoder.encode("admin123"), UserRole.ADMINISTRATOR, "System Administrator"));
        log.info("Seeded 4 demo users: manager / operator / tech / admin");
    }

    private void seedThresholds() {
        if (thresholdRepository.count() > 0) {
            return;
        }
        Instant now = Instant.now();
        // sensorType, min, warnMin, warnMax, max, unit
        thresholdRepository.save(new ThresholdConfiguration(
                SensorType.WEIGHT, 90.0, 95.0, 105.0, 110.0, "g", now));
        thresholdRepository.save(new ThresholdConfiguration(
                SensorType.TEMPERATURE, 15.0, 18.0, 30.0, 35.0, "C", now));
        thresholdRepository.save(new ThresholdConfiguration(
                SensorType.VIBRATION, 0.0, 0.0, 5.0, 8.0, "mm/s", now));
        log.info("Seeded default thresholds for WEIGHT, TEMPERATURE, VIBRATION");
    }
}
