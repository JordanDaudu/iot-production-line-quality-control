package com.smartiot.qualityinspection.auth.service;

import com.smartiot.qualityinspection.auth.dto.UserDto;
import com.smartiot.qualityinspection.auth.model.UserAccount;
import com.smartiot.qualityinspection.auth.repository.UserAccountRepository;
import com.smartiot.qualityinspection.common.enums.UserRole;
import com.smartiot.qualityinspection.common.exception.ResourceNotFoundException;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Admin role management (FR-23). Lists users and updates a user's role. Guards against
 * removing the last administrator so the system can't be locked out.
 */
@Service
public class UserAccountService {

    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public List<UserDto> listUsers() {
        return userAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(UserAccount::getUsername))
                .map(UserDto::from)
                .toList();
    }

    public UserDto updateRole(Long userId, UserRole newRole) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id " + userId));

        if (account.getRole() == UserRole.ADMINISTRATOR && newRole != UserRole.ADMINISTRATOR
                && countAdministrators() <= 1) {
            throw new ValidationException("Cannot remove the last administrator.");
        }

        account.setRole(newRole);
        UserDto dto = UserDto.from(userAccountRepository.save(account));
        log.info("Role for user {} changed to {}", account.getUsername(), newRole);
        return dto;
    }

    private long countAdministrators() {
        return userAccountRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ADMINISTRATOR)
                .count();
    }
}
