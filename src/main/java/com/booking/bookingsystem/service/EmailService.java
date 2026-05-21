package com.booking.bookingsystem.service;

import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${booking.email.from:${spring.mail.username:no-reply@booking.local}}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        sendBookingEmail(
                booking,
                "booking-confirmation",
                "Booking Confirmation - " + booking.getBookingReference()
        );
    }

    @Async
    public void sendBookingCancellation(Booking booking) {
        sendBookingEmail(
                booking,
                "booking-cancellation",
                "Booking Cancelled - " + booking.getBookingReference()
        );
    }

    @Async
    public void sendBookingReminder(Booking booking) {
        sendBookingEmail(
                booking,
                "booking-reminder",
                "Reminder: Upcoming Booking - " + booking.getBookingReference()
        );
    }

    // NEW METHOD: Send welcome email after registration
    @Async
    public void sendWelcomeEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Skipping welcome email because user email is missing");
            return;
        }

        try {
            log.info("Sending welcome email to: {}", user.getEmail());

            Context context = new Context();
            context.setVariable("userName", user.getFullName());
            context.setVariable("email", user.getEmail());
            context.setVariable("role", user.getRole().name());
            context.setVariable("loginUrl", "http://localhost:8080/swagger-ui.html");

            String htmlContent = templateEngine.process("welcome-email", context);
            sendEmail(user.getEmail(), "Welcome to Booking System!", htmlContent);

            log.info("Welcome email sent successfully to {}", user.getEmail());
        } catch (MailException | MessagingException ex) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), ex.getMessage(), ex);
        }
    }

    private void sendBookingEmail(Booking booking, String templateName, String subject) {
        if (booking.getUser() == null || booking.getUser().getEmail() == null || booking.getUser().getEmail().isBlank()) {
            log.warn("Skipping {} email for booking {} because user email is missing",
                    templateName, booking.getBookingReference());
            return;
        }

        try {
            Context context = new Context();
            buildBookingVariables(booking).forEach(context::setVariable);

            String htmlContent = templateEngine.process(templateName, context);
            sendEmail(booking.getUser().getEmail(), subject, htmlContent);
            log.info("{} email sent to {} for booking {}",
                    templateName, booking.getUser().getEmail(), booking.getBookingReference());
        } catch (MailException | MessagingException ex) {
            log.error("Failed to send {} email for booking {}: {}",
                    templateName, booking.getBookingReference(), ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildBookingVariables(Booking booking) {
        return Map.of(
                "userName", booking.getUser().getFullName(),
                "bookingReference", booking.getBookingReference(),
                "resourceCode", booking.getResource().getResourceCode(),
                "location", booking.getResource().getLocation() == null ? "" : booking.getResource().getLocation(),
                "startTime", booking.getStartTime(),
                "endTime", booking.getEndTime(),
                "totalPrice", booking.getTotalPrice() == null ? 0.0d : booking.getTotalPrice(),
                "status", booking.getStatus()
        );
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}