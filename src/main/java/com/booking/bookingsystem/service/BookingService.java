package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.request.BookingRequest;
import com.booking.bookingsystem.dto.response.BookingResponse;
import com.booking.bookingsystem.entity.Booking;
import com.booking.bookingsystem.entity.Resource;
import com.booking.bookingsystem.entity.User;
import com.booking.bookingsystem.enums.BookingStatus;
import com.booking.bookingsystem.enums.PaymentStatus;
import com.booking.bookingsystem.enums.ResourceStatus;
import com.booking.bookingsystem.enums.UserStatus;
import com.booking.bookingsystem.exception.BusinessException;
import com.booking.bookingsystem.exception.ResourceNotFoundException;
import com.booking.bookingsystem.mapper.BookingMapper;
import com.booking.bookingsystem.repository.BookingRepository;
import com.booking.bookingsystem.repository.ResourceRepository;
import com.booking.bookingsystem.repository.UserRepository;
import com.booking.bookingsystem.util.PriceCalculator;
import com.booking.bookingsystem.util.ReferenceGenerator;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final ReferenceGenerator referenceGenerator;

    @Retryable(
            value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        log.info("Creating booking for user {} and resource {}", userId, request.getResourceId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("USER_INACTIVE", "User account is not active");
        }

        Resource resource = resourceRepository.findByIdWithLock(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource", request.getResourceId()));
        if (resource.getStatus() != ResourceStatus.AVAILABLE) {
            throw new BusinessException("RESOURCE_NOT_AVAILABLE", "Resource " + resource.getResourceCode() + " is not available");
        }

        validateResourceAvailability(resource.getId(), request.getStartTime(), request.getEndTime());

        if (request.getQuantity() != null && resource.getCapacity() != null
                && request.getQuantity() > resource.getCapacity()) {
            throw new BusinessException("EXCEEDS_CAPACITY", "Resource capacity is " + resource.getCapacity());
        }

        Booking booking = Booking.builder()
                .bookingReference(referenceGenerator.generateBookingReference())
                .user(user)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .quantity(request.getQuantity())
                .notes(request.getNotes())
                .totalPrice(PriceCalculator.calculate(resource, request.getStartTime(), request.getEndTime()))
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Process payment if present
        boolean paymentProcessed = false;
        if (request.getPayment() != null) {
            log.info("Processing payment for booking: {}", savedBooking.getBookingReference());
            paymentService.processPayment(savedBooking.getId(), request.getPayment());
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            savedBooking = bookingRepository.save(savedBooking);
            paymentProcessed = true;
            log.info("Payment completed for booking: {}", savedBooking.getBookingReference());
        }

        // Send confirmation email ONLY AFTER payment is processed (or if no payment needed)
        if (paymentProcessed || request.getPayment() == null) {
            notificationService.sendBookingConfirmation(savedBooking);
        }

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public void validateResourceAvailability(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(resourceId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("RESOURCE_ALREADY_BOOKED", "Resource is already booked for selected time");
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "bookings", key = "#bookingReference")
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));
        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toResponse);
    }

    @CacheEvict(value = "bookings", key = "#bookingReference")
    public BookingResponse cancelBooking(String bookingReference, Long userId) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "You can only cancel your own bookings");
        }
        if (LocalDateTime.now().isAfter(booking.getStartTime().minusHours(24))) {
            throw new BusinessException("LATE_CANCELLATION", "Cancellation allowed at least 24 hours before start");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("INVALID_STATUS", "Cannot cancel booking with status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        if (booking.getPayment() != null && booking.getPayment().getStatus() == PaymentStatus.COMPLETED) {
            paymentService.processRefund(booking.getId());
        }
        notificationService.sendBookingCancellation(updatedBooking);
        return bookingMapper.toResponse(updatedBooking);
    }

    public BookingResponse startBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("INVALID_STATUS", "Start only allowed for confirmed bookings");
        }
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.getResource().setStatus(ResourceStatus.BOOKED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking {} started", bookingReference);
        return bookingMapper.toResponse(savedBooking);
    }

    public BookingResponse completeBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new BusinessException("INVALID_STATUS", "End only allowed for in-progress bookings");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking.getResource().setStatus(ResourceStatus.AVAILABLE);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking {} completed", bookingReference);
        return bookingMapper.toResponse(savedBooking);
    }
}