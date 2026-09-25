package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateUserRequest;
import com.max.erp.identity.dto.UserResponseDto;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user.
     *
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = userService.createUser(request);

        UserResponseDto response = toResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get all users.
     *
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        List<UserResponseDto> response = userService.getAllUsers()
                .stream()
                .map(this::toResponseDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get the currently authenticated user.
     *
     * GET /api/v1/users/me
     *
     * The username comes from the authenticated Spring Security
     * principal rather than from a request parameter.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        String username = authentication.getName();

        User user = userService.getByUsername(username);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponseDto(user));
    }

    /**
     * Convert User entity to API response DTO.
     *
     * Password hash is intentionally never returned.
     */
    private UserResponseDto toResponseDto(User user) {

        UserResponseDto response = new UserResponseDto();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.isEmailVerified());
        response.setMfaEnabled(user.isMfaEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}