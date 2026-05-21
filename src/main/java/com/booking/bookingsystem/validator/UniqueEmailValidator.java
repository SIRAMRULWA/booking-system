package com.booking.bookingsystem.validator;

import com.booking.bookingsystem.exception.ConflictException;
import com.booking.bookingsystem.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator {

    private final UserRepository userRepository;

    public UniqueEmailValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validate(String email) {
        if (email != null && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
    }
}
