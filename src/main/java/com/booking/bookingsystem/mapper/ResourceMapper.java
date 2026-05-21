package com.booking.bookingsystem.mapper;

import com.booking.bookingsystem.dto.response.ResourceResponse;
import com.booking.bookingsystem.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .resourceCode(resource.getResourceCode())
                .category(resource.getCategory())
                .location(resource.getLocation())
                .capacity(resource.getCapacity())
                .price(resource.getPrice())
                .description(resource.getDescription())
                .features(resource.getFeatures())
                .status(resource.getStatus() == null ? null : resource.getStatus().name())
                .mode(resource.getMode() == null ? null : resource.getMode().name())
                .slotStart(resource.getSlotStart() == null ? null : resource.getSlotStart().toString())
                .slotEnd(resource.getSlotEnd() == null ? null : resource.getSlotEnd().toString())
                .slotDate(resource.getSlotDate() == null ? null : resource.getSlotDate().toString())
                .recurrence(resource.getRecurrence())
                .build();
    }
}