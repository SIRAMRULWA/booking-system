package com.booking.bookingsystem.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private UserSummary user;
    private ResourceSummary resource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private Double totalPrice;
    private String status;
    private String notes;
    private PaymentSummary payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceSummary {
        private Long id;
        private String resourceCode;
        private String category;
        private Double price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private Long id;
        private String transactionId;
        private Double amount;
        private String method;
        private String status;
    }
}
