package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateUserRequest;
import com.max.erp.identity.dto.UpdateUserRequest;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    private UUID userId;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController controller = new UserController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@maxerp.local");
        sampleUser.setStatus("ACTIVE");
        sampleUser.setEmailVerified(true);
        sampleUser.setMfaEnabled(false);
        ReflectionTestUtils.setField(sampleUser, "id", userId);
        ReflectionTestUtils.setField(sampleUser, "createdAt", Instant.now());
        ReflectionTestUtils.setField(sampleUser, "updatedAt", Instant.now());
    }

    @Test
    @DisplayName("GET /api/v1/users returns 200 with user list")
    void getAllUsersReturns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@maxerp.local"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} returns 200 with user detail")
    void getUserByIdReturns200() throws Exception {
        when(userService.getUserById(userId)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/v1/users creates user and returns 201")
    void createUserReturns201() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "newuser",
                            "email": "newuser@maxerp.local",
                            "password": "Password@123"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@maxerp.local"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{userId} updates user and returns 200")
    void updateUserReturns200() throws Exception {
        User updatedUser = new User();
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("updated@maxerp.local");
        updatedUser.setStatus("INACTIVE");
        ReflectionTestUtils.setField(updatedUser, "id", userId);
        ReflectionTestUtils.setField(updatedUser, "createdAt", Instant.now());
        ReflectionTestUtils.setField(updatedUser, "updatedAt", Instant.now());

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "updated@maxerp.local",
                            "status": "INACTIVE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@maxerp.local"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}
