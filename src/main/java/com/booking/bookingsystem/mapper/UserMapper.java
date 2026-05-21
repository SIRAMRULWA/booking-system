package com.booking.bookingsystem.mapper;

import com.booking.bookingsystem.dto.response.UserResponse;
import com.booking.bookingsystem.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .role(user.getRole().name())
            .status(user.getStatus().name())
            .build();
    }
}
