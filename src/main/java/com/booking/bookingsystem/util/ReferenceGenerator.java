package com.booking.bookingsystem.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReferenceGenerator {

    public String generateBookingReference() {
        return "BKG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
