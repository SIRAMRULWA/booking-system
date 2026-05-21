package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.response.UserResponse;
import com.booking.bookingsystem.exception.ResourceNotFoundException;
import com.booking.bookingsystem.mapper.UserMapper;
import com.booking.bookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUser(Long id) {
        return userMapper.toResponse(userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }
}
