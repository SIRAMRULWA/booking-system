package com.booking.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Payment method is required")
    private String method;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @Pattern(regexp = "^\\d{16}$|^$", message = "Invalid card number")
    private String cardNumber;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$|^$", message = "Invalid expiry date format (MM/YY)")
    private String expiryDate;

    @Pattern(regexp = "^\\d{3,4}$|^$", message = "Invalid CVV")
    private String cvv;
}
