package com.booking.bookingsystem.dto.request;

import com.booking.bookingsystem.enums.ResourceMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "Request body for creating a bookable resource")
public class ResourceRequest {

    @NotBlank
    @Schema(example = "RESOURCE-002")
    private String resourceCode;

    @NotBlank
    @Schema(example = "meeting_room")
    private String category;

    @Size(max = 255)
    @Schema(example = "Executive Room")
    private String location;

    @Positive
    @Schema(example = "20")
    private Integer capacity;

    @Positive
    @Schema(example = "150.00")
    private Double price;

    @Schema(example = "Executive meeting room with display and video conferencing")
    private String description;

    @Schema(example = "[\"Projector\", \"Whiteboard\", \"Video conferencing\"]")
    private List<String> features;

    @NotNull(message = "Mode is required")
    @Schema(description = "FLEXIBLE (user picks time) or FIXED (admin defines slot)",
            example = "FLEXIBLE", allowableValues = {"FLEXIBLE", "FIXED"})
    private ResourceMode mode;

    // FIXED mode fields (required if mode = FIXED)
    @Schema(description = "Start time for FIXED mode slots", example = "09:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime slotStart;

    @Schema(description = "End time for FIXED mode slots", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime slotEnd;

    @Schema(description = "Date for FIXED mode slots", example = "2026-05-20")
    private LocalDate slotDate;

    @Schema(description = "Recurrence pattern for FIXED mode",
            example = "WEEKLY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
    private String recurrence;
}