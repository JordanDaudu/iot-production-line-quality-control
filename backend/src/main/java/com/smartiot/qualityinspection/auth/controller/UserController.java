package com.smartiot.qualityinspection.auth.controller;

import com.smartiot.qualityinspection.auth.dto.UpdateUserRoleRequest;
import com.smartiot.qualityinspection.auth.dto.UserDto;
import com.smartiot.qualityinspection.auth.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administrator-only user and role management (FR-23). Enforced server-side (NFR-11).
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserController {

    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public List<UserDto> list() {
        return userAccountService.listUsers();
    }

    @PutMapping("/{id}/role")
    public UserDto updateRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return userAccountService.updateRole(id, request.role());
    }
}
