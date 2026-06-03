package com.smartiot.qualityinspection.auth.dto;

import com.smartiot.qualityinspection.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * Request to change a user's role (FR-23).
 */
public record UpdateUserRoleRequest(
        @NotNull UserRole role
) {
}
