package com.booking.bookingsystem.config;

import com.booking.bookingsystem.entity.User;
import com.booking.bookingsystem.enums.UserRole;
import com.booking.bookingsystem.enums.UserStatus;
import com.booking.bookingsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableRetry
@EnableAsync
@EnableScheduling
@Slf4j
public class AppConfig {

    @Bean
    CommandLineRunner bootstrapAdminUser(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${booking.admin.email:admin@booking.local}") String adminEmail,
        @Value("${booking.admin.password:AdminPass123!}") String adminPassword,
        @Value("${booking.admin.full-name:System Admin}") String adminFullName
    ) {
        return args -> {
            boolean adminEmailExists = userRepository.findByEmail(adminEmail)
                .map(existingUser -> {
                    existingUser.setPassword(passwordEncoder.encode(adminPassword));
                    existingUser.setFullName(adminFullName);
                    existingUser.setRole(UserRole.ADMIN);
                    existingUser.setStatus(UserStatus.ACTIVE);
                    userRepository.save(existingUser);
                    return true;
                })
                .orElse(false);

            if (!adminEmailExists && !userRepository.existsByRole(UserRole.ADMIN)) {
                userRepository.save(User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName(adminFullName)
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());
            }

            log.info("Bootstrapped admin user {}", adminEmail);
        };
    }
}
