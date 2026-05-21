package com.booking.bookingsystem.filter;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void usesIncomingRequestIdWhenValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/resources");
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("request-123");
        assertThat(request.getAttribute(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("request-123");
        assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isNull();
    }

    @Test
    void generatesRequestIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/resources");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotBlank();
        assertThat(request.getAttribute(RequestIdFilter.REQUEST_ID_HEADER))
            .isEqualTo(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER));
    }

    @Test
    void replacesUnsafeRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/resources");
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "bad request id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotEqualTo("bad request id");
        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotBlank();
    }
}
