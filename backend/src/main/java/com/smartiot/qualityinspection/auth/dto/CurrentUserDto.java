package com.smartiot.qualityinspection.auth.dto;

/**
 * The authenticated user's identity and role, returned by GET /api/auth/me.
 * The frontend uses {@code role} to show/hide role-restricted controls.
 *
 * @param username    login name
 * @param displayName friendly name for the dashboard
 * @param role        one of QUALITY_MANAGER, OPERATOR, MAINTENANCE_TECHNICIAN, ADMINISTRATOR
 */
public record CurrentUserDto(
        String username,
        String displayName,
        String role
) {
}
