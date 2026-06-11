package com.booking.bookingsystem.service;

import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.entity.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final TemplateEngine templateEngine;
    private final SendGrid sendGrid;
    private final String fromEmail;

    public EmailService(TemplateEngine templateEngine,
                        @Value("${sendgrid.api-key}") String apiKey,
                        @Value("${mail.from}") String fromEmail) {
        this.templateEngine = templateEngine;
        this.sendGrid = new SendGrid(apiKey);
        this.fromEmail = fromEmail;
    }

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
            context.setVariable("loginUrl", "https://booking-system-api-b4m0.onrender.com/swagger-ui.html");

            String htmlContent = templateEngine.process("welcome-email", context);
            sendEmail(user.getEmail(), "Welcome to Booking System!", htmlContent);

            log.info("Welcome email sent successfully to {}", user.getEmail());
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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

    private void sendEmail(String to, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, toEmail, content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            log.info("Email sent to {} (status: {})", to, response.getStatusCode());
        } else {
            log.warn("Email sending returned {}: {}", response.getStatusCode(), response.getBody());
        }
    }
}