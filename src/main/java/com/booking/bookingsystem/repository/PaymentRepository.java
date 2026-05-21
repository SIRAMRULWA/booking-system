package com.booking.bookingsystem.repository;

import com.booking.bookingsystem.entity.Payment;
import com.booking.bookingsystem.enums.PaymentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByBookingId(Long bookingId);
    long countByStatus(PaymentStatus status);
}
