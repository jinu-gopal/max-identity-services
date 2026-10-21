package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateUserRequest;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user.
     *
     * The original password is never stored.
     * Only the BCrypt password hash is persisted.
     */
    public User createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Never store the original password.
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        user.setStatus("ACTIVE");
        user.setEmailVerified(false);
        user.setMfaEnabled(false);

        return userRepository.save(user);
    }

    /**
     * Get a user by UUID.
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    /**
     * Get the currently authenticated user by username.
     *
     * This is used by the /api/v1/users/me endpoint
     * so the client does not need to provide a user ID.
     */
    @Transactional(readOnly = true)
    public User getByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    /**
     * Update an existing user.
     */
    public User updateUser(UUID id, User updatedUser) {

        User existingUser = getUserById(id);

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setStatus(updatedUser.getStatus());
        existingUser.setEmailVerified(
                updatedUser.isEmailVerified()
        );
        existingUser.setMfaEnabled(
                updatedUser.isMfaEnabled()
        );

        return userRepository.save(existingUser);
    }

    /**
     * Update user via request DTO.
     */
    public User updateUser(UUID id, com.max.erp.identity.dto.UpdateUserRequest request) {
        User existingUser = getUserById(id);

        if (request.email() != null && !request.email().isBlank()) {
            existingUser.setEmail(request.email().trim());
        }

        if (request.status() != null && !request.status().isBlank()) {
            existingUser.setStatus(request.status().trim().toUpperCase());
        }

        if (request.emailVerified() != null) {
            existingUser.setEmailVerified(request.emailVerified());
        }

        if (request.mfaEnabled() != null) {
            existingUser.setMfaEnabled(request.mfaEnabled());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Delete a user.
     */
    public void deleteUser(UUID id) {

        User user = getUserById(id);

        userRepository.delete(user);
    }
}