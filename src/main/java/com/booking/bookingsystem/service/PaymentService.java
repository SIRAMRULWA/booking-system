package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.request.PaymentRequest;
import com.booking.bookingsystem.dto.response.PaymentResponse;
import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.entity.Payment;
import com.booking.bookingsystem.enums.BookingStatus;
import com.booking.bookingsystem.enums.PaymentMethod;
import com.booking.bookingsystem.enums.PaymentStatus;
import com.booking.bookingsystem.exception.ResourceNotFoundException;
import com.booking.bookingsystem.mapper.PaymentMapper;
import com.booking.bookingsystem.repository.BookingRepository;
import com.booking.bookingsystem.repository.PaymentRepository;
import com.booking.bookingsystem.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final ReferenceGenerator referenceGenerator;

    public PaymentResponse processPayment(Long bookingId, PaymentRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        Payment payment = Payment.builder()
                .booking(booking)
                .transactionId(referenceGenerator.generateTransactionId())
                .amount(request.getAmount())
                .method(PaymentMethod.valueOf(request.getMethod().toUpperCase()))
                .status(PaymentStatus.COMPLETED)
                .build();
        payment = paymentRepository.save(payment);
        booking.setPayment(payment);

        // Update booking status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("Payment processed for booking {}: {}, booking status updated to CONFIRMED", bookingId, payment.getTransactionId());
        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse processRefund(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status back to PENDING when refunded
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        log.info("Refund processed for booking {}: {}, booking status updated to PENDING", bookingId, payment.getTransactionId());
        return paymentMapper.toResponse(savedPayment);
    }
}