package com.booking.bookingsystem.service;

import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.repository.BookingRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReminderService {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    private final Cache<String, Boolean> sentReminders = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofDays(2))
        .maximumSize(10_000)
        .build();

    @Value("${booking.email.reminders-enabled:true}")
    private boolean remindersEnabled;

    @Value("${booking.email.reminder-hours-before:24}")
    private long reminderHoursBefore;

    @Value("${booking.email.reminder-window-minutes:60}")
    private long reminderWindowMinutes;

    @Scheduled(fixedDelayString = "${booking.email.reminder-check-delay-ms:3600000}")
    public void sendUpcomingBookingReminders() {
        if (!remindersEnabled) {
            return;
        }

        LocalDateTime windowStart = LocalDateTime.now().plusHours(reminderHoursBefore);
        LocalDateTime windowEnd = windowStart.plusMinutes(reminderWindowMinutes);
        List<Booking> upcomingBookings = bookingRepository.findUpcomingStarts(windowStart, windowEnd);

        for (Booking booking : upcomingBookings) {
            String key = booking.getBookingReference();
            if (sentReminders.getIfPresent(key) != null) {
                continue;
            }

            notificationService.sendBookingReminder(booking);
            sentReminders.put(key, true);
        }

        if (!upcomingBookings.isEmpty()) {
            log.info("Queued {} booking reminder email(s)", upcomingBookings.size());
        }
    }
}
