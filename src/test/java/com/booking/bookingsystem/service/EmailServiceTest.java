package com.booking.bookingsystem.service;

import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.entity.Resource;
import com.booking.bookingsystem.entity.User;
import com.booking.bookingsystem.enums.BookingStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@example.com");
    }

    @Test
    void sendBookingConfirmationRendersTemplateAndSendsEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-confirmation"), any())).thenReturn("<p>confirmed</p>");

        emailService.sendBookingConfirmation(booking());

        verify(templateEngine).process(eq("booking-confirmation"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendBookingCancellationRendersTemplateAndSendsEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-cancellation"), any())).thenReturn("<p>cancelled</p>");

        emailService.sendBookingCancellation(booking());

        verify(templateEngine).process(eq("booking-cancellation"), any());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendBookingReminderRendersTemplateAndSendsEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-reminder"), any())).thenReturn("<p>reminder</p>");

        emailService.sendBookingReminder(booking());

        verify(templateEngine).process(eq("booking-reminder"), any());
        verify(mailSender).send(mimeMessage);
    }

    private Booking booking() {
        User user = User.builder()
            .email("customer@example.com")
            .fullName("Test Customer")
            .build();

        Resource resource = Resource.builder()
            .resourceCode("ROOM-101")
            .location("Cape Town")
            .build();

        return Booking.builder()
            .bookingReference("BKG12345678")
            .user(user)
            .resource(resource)
            .startTime(LocalDateTime.now().plusDays(1))
            .endTime(LocalDateTime.now().plusDays(2))
            .totalPrice(150.0d)
            .status(BookingStatus.CONFIRMED)
            .build();
    }
}
