package com.smartiot.qualityinspection.auth.controller;

import com.smartiot.qualityinspection.auth.dto.CurrentUserDto;
import com.smartiot.qualityinspection.auth.model.UserAccount;
import com.smartiot.qualityinspection.auth.repository.UserAccountRepository;
import com.smartiot.qualityinspection.common.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight auth endpoint. The frontend calls this after the user supplies
 * credentials to confirm them and learn the active role.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userAccountRepository;

    public AuthController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/me")
    public CurrentUserDto me(Authentication authentication) {
        UserAccount account = userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
        return new CurrentUserDto(account.getUsername(), account.getDisplayName(), account.getRole().name());
    }
}
