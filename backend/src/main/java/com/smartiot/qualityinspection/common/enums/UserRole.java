package com.smartiot.qualityinspection.common.enums;

/**
 * Project actor roles. Used by Spring Security for authorization. The Spring authority
 * is "ROLE_" + name(), e.g. ROLE_ADMINISTRATOR.
 */
public enum UserRole {
    QUALITY_MANAGER,
    OPERATOR,
    MAINTENANCE_TECHNICIAN,
    ADMINISTRATOR
}
