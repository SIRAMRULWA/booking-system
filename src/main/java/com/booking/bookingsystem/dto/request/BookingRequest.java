package com.booking.bookingsystem.dto.request;

import com.booking.bookingsystem.validator.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@ValidDateRange
public class BookingRequest {

    @NotNull(message = "Resource ID is required")
    @Positive(message = "Resource ID must be positive")
    private Long resourceId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    private Integer quantity;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Valid
    private PaymentRequest payment;
}
