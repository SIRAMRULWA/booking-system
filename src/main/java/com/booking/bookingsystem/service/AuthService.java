package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.request.LoginRequest;
import com.booking.bookingsystem.dto.request.RegisterRequest;
import com.booking.bookingsystem.dto.response.AuthResponse;
import com.booking.bookingsystem.entity.User;
import com.booking.bookingsystem.enums.UserRole;
import com.booking.bookingsystem.enums.UserStatus;
import com.booking.bookingsystem.exception.BusinessException;
import com.booking.bookingsystem.repository.UserRepository;
import com.booking.bookingsystem.security.JwtTokenProvider;
import com.booking.bookingsystem.validator.UniqueEmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UniqueEmailValidator uniqueEmailValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;  // ADD THIS

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        uniqueEmailValidator.validate(request.getEmail());

        // Use role from request, default to CUSTOMER if not provided
        UserRole role = request.getRole() != null ? request.getRole() : UserRole.CUSTOMER;

        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build());

        log.info("User registered successfully with role: {}", role);

        // Send welcome email after successful registration
        notificationService.sendWelcomeEmail(user);

        return AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(user.getId()))
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("USER_INACTIVE", "User account is not active");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(user.getId()))
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}