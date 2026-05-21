package com.booking.bookingsystem.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class DateUtils {
    private DateUtils() {
    }

    public static long chargeableUnitsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        long hours = ChronoUnit.HOURS.between(startTime, endTime);
        return Math.max(1, hours);
    }
}
