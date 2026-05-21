package com.booking.bookingsystem.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String resourceCode;
    private String category;
    private String location;
    private Integer capacity;
    private Double price;
    private String description;
    private List<String> features;
    private String status;
    private String mode;           // FLEXIBLE or FIXED
    private String slotStart;      // For FIXED mode
    private String slotEnd;        // For FIXED mode
    private String slotDate;       // For FIXED mode
    private String recurrence;     // For FIXED mode
}