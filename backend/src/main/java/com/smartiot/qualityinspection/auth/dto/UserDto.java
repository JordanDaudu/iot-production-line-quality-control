package com.smartiot.qualityinspection.auth.dto;

import com.smartiot.qualityinspection.auth.model.UserAccount;
import com.smartiot.qualityinspection.common.enums.UserRole;

/**
 * A user account for the admin role-management screen (FR-23). Never exposes the password.
 */
public record UserDto(
        Long id,
        String username,
        String displayName,
        UserRole role
) {

    public static UserDto from(UserAccount account) {
        return new UserDto(account.getId(), account.getUsername(), account.getDisplayName(), account.getRole());
    }
}
