package com.booking.bookingsystem.mapper;

import com.booking.bookingsystem.dto.response.BookingResponse;
import com.booking.bookingsystem.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        BookingResponse.PaymentSummary paymentSummary = null;
        if (booking.getPayment() != null) {
            paymentSummary = BookingResponse.PaymentSummary.builder()
                .id(booking.getPayment().getId())
                .transactionId(booking.getPayment().getTransactionId())
                .amount(booking.getPayment().getAmount())
                .method(booking.getPayment().getMethod() == null ? null : booking.getPayment().getMethod().name())
                .status(booking.getPayment().getStatus() == null ? null : booking.getPayment().getStatus().name())
                .build();
        }

        return BookingResponse.builder()
            .id(booking.getId())
            .bookingReference(booking.getBookingReference())
            .user(BookingResponse.UserSummary.builder()
                .id(booking.getUser().getId())
                .fullName(booking.getUser().getFullName())
                .email(booking.getUser().getEmail())
                .build())
            .resource(BookingResponse.ResourceSummary.builder()
                .id(booking.getResource().getId())
                .resourceCode(booking.getResource().getResourceCode())
                .category(booking.getResource().getCategory())
                .price(booking.getResource().getPrice())
                .build())
            .startTime(booking.getStartTime())
            .endTime(booking.getEndTime())
            .quantity(booking.getQuantity())
            .totalPrice(booking.getTotalPrice())
            .status(booking.getStatus().name())
            .notes(booking.getNotes())
            .payment(paymentSummary)
            .createdAt(booking.getCreatedAt())
            .updatedAt(booking.getUpdatedAt())
            .build();
    }
}
