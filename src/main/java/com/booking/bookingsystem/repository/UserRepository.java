package com.booking.bookingsystem.repository;

import com.booking.bookingsystem.entity.User;
import com.booking.bookingsystem.enums.UserRole;
import com.booking.bookingsystem.enums.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByRole(UserRole role);
    long countByStatus(UserStatus status);
}
