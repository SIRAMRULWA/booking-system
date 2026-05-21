package com.booking.bookingsystem.util;

import com.booking.bookingsystem.entity.Resource;
import java.time.LocalDateTime;

public final class PriceCalculator {
    private PriceCalculator() {
    }

    public static double calculate(Resource resource, LocalDateTime startTime, LocalDateTime endTime) {
        return resource.getPrice() * DateUtils.chargeableUnitsBetween(startTime, endTime);
    }
}
