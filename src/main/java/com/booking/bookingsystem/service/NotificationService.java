package com.booking.bookingsystem.service;

import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        emailService.sendBookingConfirmation(booking);
    }

    @Async
    public void sendBookingCancellation(Booking booking) {
        emailService.sendBookingCancellation(booking);
    }

    @Async
    public void sendBookingReminder(Booking booking) {
        emailService.sendBookingReminder(booking);
    }

    // Add this method
    @Async
    public void sendWelcomeEmail(User user) {
        emailService.sendWelcomeEmail(user);
    }
}