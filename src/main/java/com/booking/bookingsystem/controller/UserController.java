package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.dto.response.UserResponse;
import com.booking.bookingsystem.security.CurrentUser;
import com.booking.bookingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@CurrentUser Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}
