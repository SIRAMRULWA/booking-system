package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.request.ResourceRequest;
import com.booking.bookingsystem.dto.response.ResourceResponse;
import com.booking.bookingsystem.entity.Resource;
import com.booking.bookingsystem.enums.ResourceMode;
import com.booking.bookingsystem.enums.ResourceStatus;
import com.booking.bookingsystem.exception.ConflictException;
import com.booking.bookingsystem.exception.ResourceNotFoundException;
import com.booking.bookingsystem.mapper.ResourceMapper;
import com.booking.bookingsystem.repository.ResourceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public Page<ResourceResponse> getResources(Pageable pageable) {
        return resourceRepository.findAll(pageable).map(resourceMapper::toResponse);
    }

    public Page<ResourceResponse> getAvailableResources(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        LocalDate date = startTime.toLocalDate();
        LocalTime startTimeOnly = startTime.toLocalTime();
        LocalTime endTimeOnly = endTime.toLocalTime();

        return resourceRepository.findAvailableResources(startTime, endTime, date, startTimeOnly, endTimeOnly, pageable)
            .map(resourceMapper::toResponse);
    }

    // Alternative: Get all FIXED slots for a specific date (for calendar view)
    public Page<ResourceResponse> getFixedResourcesByDate(LocalDate date, Pageable pageable) {
        return resourceRepository.findFixedResourcesByDate(date, pageable)
                .map(resourceMapper::toResponse);
    }

    public ResourceResponse createResource(ResourceRequest request) {
        if (resourceRepository.findByResourceCode(request.getResourceCode()).isPresent()) {
            throw new ConflictException("Resource code already exists: " + request.getResourceCode());
        }

        // Validate FIXED mode fields
        if (request.getMode() == ResourceMode.FIXED) {
            if (request.getSlotStart() == null || request.getSlotEnd() == null || request.getSlotDate() == null) {
                throw new IllegalArgumentException("FIXED mode requires slotStart, slotEnd, and slotDate");
            }
        }

        Resource resource = Resource.builder()
                .resourceCode(request.getResourceCode())
                .category(request.getCategory())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .price(request.getPrice())
                .description(request.getDescription())
                .features(request.getFeatures() == null ? new ArrayList<>() : request.getFeatures())
                .status(ResourceStatus.AVAILABLE)
                .mode(request.getMode())
                .slotStart(request.getMode() == ResourceMode.FIXED ? request.getSlotStart() : null)
                .slotEnd(request.getMode() == ResourceMode.FIXED ? request.getSlotEnd() : null)
                .slotDate(request.getMode() == ResourceMode.FIXED ? request.getSlotDate() : null)
                .recurrence(request.getRecurrence())
                .build();
        return resourceMapper.toResponse(resourceRepository.save(resource));
    }

    public ResourceResponse getResource(Long id) {
        return resourceMapper.toResponse(resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", id)));
    }
}
