package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.dto.request.ResourceRequest;
import com.booking.bookingsystem.dto.response.CustomPageResponse;
import com.booking.bookingsystem.dto.response.ResourceResponse;
import com.booking.bookingsystem.service.ResourceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<CustomPageResponse<ResourceResponse>> getResources(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ResourceResponse> page = resourceService.getResources(pageable);
        return ResponseEntity.ok(CustomPageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.createResource(request));
    }

    @GetMapping("/availability")
    public ResponseEntity<CustomPageResponse<ResourceResponse>> getAvailableResources(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ResourceResponse> page = resourceService.getAvailableResources(startTime, endTime, pageable);
        return ResponseEntity.ok(CustomPageResponse.from(page));
    }

    // Optional: Get all FIXED slots for a specific date (for calendar view)
    @GetMapping("/fixed-slots")
    public ResponseEntity<CustomPageResponse<ResourceResponse>> getFixedSlotsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ResourceResponse> page = resourceService.getFixedResourcesByDate(date, pageable);
        return ResponseEntity.ok(CustomPageResponse.from(page));
    }
}
