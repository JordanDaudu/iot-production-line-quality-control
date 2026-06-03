package com.smartiot.qualityinspection.auth.service;

import com.smartiot.qualityinspection.auth.dto.UserDto;
import com.smartiot.qualityinspection.auth.model.UserAccount;
import com.smartiot.qualityinspection.auth.repository.UserAccountRepository;
import com.smartiot.qualityinspection.common.enums.UserRole;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for role management (FR-23), including the last-administrator safeguard.
 */
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserAccountService service;

    @Test
    void updatesRole() {
        UserAccount operator = new UserAccount("operator", "x", UserRole.OPERATOR, "Operator");
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserDto dto = service.updateRole(1L, UserRole.MAINTENANCE_TECHNICIAN);

        assertEquals(UserRole.MAINTENANCE_TECHNICIAN, dto.role());
    }

    @Test
    void cannotRemoveLastAdministrator() {
        UserAccount admin = new UserAccount("admin", "x", UserRole.ADMINISTRATOR, "Admin");
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userAccountRepository.findAll()).thenReturn(List.of(admin));

        assertThrows(ValidationException.class, () -> service.updateRole(1L, UserRole.OPERATOR));
    }
}
