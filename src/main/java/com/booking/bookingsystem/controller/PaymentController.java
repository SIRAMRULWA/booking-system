package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.dto.request.PaymentRequest;
import com.booking.bookingsystem.dto.response.PaymentResponse;
import com.booking.bookingsystem.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process/{bookingId}")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long bookingId,
                                                          @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(bookingId, request));
    }

    @PostMapping("/refund/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.processRefund(bookingId));
    }
}
