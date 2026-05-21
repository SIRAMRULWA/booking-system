package com.booking.bookingsystem.mapper;

import com.booking.bookingsystem.dto.response.PaymentResponse;
import com.booking.bookingsystem.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .transactionId(payment.getTransactionId())
            .amount(payment.getAmount())
            .method(payment.getMethod() == null ? null : payment.getMethod().name())
            .status(payment.getStatus() == null ? null : payment.getStatus().name())
            .build();
    }
}
