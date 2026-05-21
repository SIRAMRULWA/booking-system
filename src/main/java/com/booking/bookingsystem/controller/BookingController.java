package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.dto.request.BookingRequest;
import com.booking.bookingsystem.dto.response.BookingResponse;
import com.booking.bookingsystem.security.CurrentUser;
import com.booking.bookingsystem.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Endpoints for managing bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Booking created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Resource already booked")
    })
    public ResponseEntity<BookingResponse> createBooking(
        @Parameter(hidden = true) @CurrentUser Long userId,
        @Valid @RequestBody BookingRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        BookingResponse response = bookingService.createBooking(userId, request);
        URI location = uriBuilder.path("/api/v1/bookings/{reference}")
            .buildAndExpand(response.getBookingReference())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{reference}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(reference));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<BookingResponse>> getUserBookings(
        @Parameter(hidden = true) @CurrentUser Long userId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, pageable));
    }

    @DeleteMapping("/{reference}")
    public ResponseEntity<Void> cancelBooking(@PathVariable String reference, @CurrentUser Long userId) {
        bookingService.cancelBooking(reference, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reference}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BookingResponse> startBooking(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.startBooking(reference));
    }

    @PostMapping("/{reference}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.completeBooking(reference));
    }
}
