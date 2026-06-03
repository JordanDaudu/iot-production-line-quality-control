package com.smartiot.qualityinspection.threshold.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.threshold.dto.ThresholdDto;
import com.smartiot.qualityinspection.threshold.dto.ThresholdUpdateRequest;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for threshold updates, including the min &le; warnMin &le; warnMax &le; max
 * validation (FR-18, TC-E2E-05).
 */
@ExtendWith(MockitoExtension.class)
class ThresholdServiceTest {

    @Mock
    private ThresholdConfigurationRepository repository;

    @InjectMocks
    private ThresholdService service;

    @Test
    void validUpdateIsSaved() {
        ThresholdConfiguration existing =
                new ThresholdConfiguration(SensorType.WEIGHT, 90.0, 95.0, 105.0, 110.0, "g", Instant.now());
        when(repository.findBySensorType(SensorType.WEIGHT)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ThresholdDto dto = service.update(
                SensorType.WEIGHT, new ThresholdUpdateRequest(90.0, 95.0, 105.0, 112.0, "g"), "ADMINISTRATOR");

        assertEquals(112.0, dto.maxValue());
        assertEquals("ADMINISTRATOR", dto.updatedByRole());
    }

    @Test
    void invalidOrderingIsRejected() {
        // min (200) greater than the rest -> rejected before touching the repository.
        assertThrows(ValidationException.class, () -> service.update(
                SensorType.WEIGHT, new ThresholdUpdateRequest(200.0, 95.0, 105.0, 110.0, "g"), "ADMINISTRATOR"));
    }
}
