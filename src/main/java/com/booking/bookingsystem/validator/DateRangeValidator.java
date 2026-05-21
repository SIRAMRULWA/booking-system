package com.booking.bookingsystem.validator;

import com.booking.bookingsystem.dto.request.BookingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, BookingRequest> {

    @Override
    public boolean isValid(BookingRequest request, ConstraintValidatorContext context) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            return true;
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Start time must be before end time")
                .addPropertyNode("startTime")
                .addConstraintViolation();
            return false;
        }
        long hours = ChronoUnit.HOURS.between(request.getStartTime(), request.getEndTime());
        if (hours < 1 || hours > 24 * 30) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Booking duration must be between 1 hour and 30 days")
                .addPropertyNode("endTime")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
