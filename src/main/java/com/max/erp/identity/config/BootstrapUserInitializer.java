package com.max.erp.identity.config;

import com.max.erp.identity.entity.User;
import com.max.erp.identity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapUserInitializer {

    @Bean
    CommandLineRunner createInitialUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String username = "admin";
            String email = "admin@maxerp.local";
            String password = "Admin@12345";

            if (userRepository.existsByUsername(username)) {
                return;
            }

            User user = new User();

            user.setUsername(username);
            user.setEmail(email);

            // Store only the BCrypt hash.
            user.setPasswordHash(
                    passwordEncoder.encode(password)
            );

            user.setStatus("ACTIVE");
            user.setEmailVerified(true);
            user.setMfaEnabled(false);

            userRepository.save(user);

            System.out.println(
                    "MAX ERP bootstrap user created: " + username
            );
        };
    }
}