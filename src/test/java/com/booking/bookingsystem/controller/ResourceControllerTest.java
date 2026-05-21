package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.dto.response.ResourceResponse;
import com.booking.bookingsystem.exception.GlobalExceptionHandler;
import com.booking.bookingsystem.service.ResourceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ResourceController(resourceService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void getResourcesReturnsPagedResponse() throws Exception {
        when(resourceService.getResources(any(Pageable.class)))
            .thenReturn(new PageImpl<>(
                List.of(ResourceResponse.builder().resourceCode("101").build()),
                PageRequest.of(0, 10),
                1
            ));

        mockMvc.perform(get("/api/v1/resources")
                .queryParam("page", "0")
                .queryParam("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].resourceCode").value("101"))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void availabilityEndpointReturnsOk() throws Exception {
        when(resourceService.getAvailableResources(any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(
                List.of(ResourceResponse.builder().resourceCode("101").build()),
                PageRequest.of(0, 10),
                1
            ));

        mockMvc.perform(get("/api/v1/resources/availability")
                .queryParam("startTime", "2030-01-01T14:00:00")
                .queryParam("endTime", "2030-01-02T11:00:00")
                .queryParam("page", "0")
                .queryParam("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].resourceCode").value("101"))
            .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void fixedSlotsEndpointReturnsPagedResponse() throws Exception {
        when(resourceService.getFixedResourcesByDate(any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(
                List.of(ResourceResponse.builder().resourceCode("FIXED-101").build()),
                PageRequest.of(0, 10),
                1
            ));

        mockMvc.perform(get("/api/v1/resources/fixed-slots")
                .queryParam("date", "2030-01-01")
                .queryParam("page", "0")
                .queryParam("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].resourceCode").value("FIXED-101"))
            .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void createResourceValidationErrorsIncludeFieldNames() throws Exception {
        String invalidRequest = """
            {
              "resourceCode": "",
              "category": "",
              "capacity": -1,
              "mode": null
            }
            """;

        mockMvc.perform(post("/api/v1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
                .header("X-Request-Id", "test-request-id"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.resourceCode").exists())
            .andExpect(jsonPath("$.fieldErrors.category").exists())
            .andExpect(jsonPath("$.fieldErrors.capacity").exists())
            .andExpect(jsonPath("$.fieldErrors.mode").value("Mode is required"))
            .andExpect(jsonPath("$.requestId").value("test-request-id"));
    }
}
