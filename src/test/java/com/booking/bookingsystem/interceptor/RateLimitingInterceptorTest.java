package com.booking.bookingsystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingInterceptorTest {

    private final RateLimitingInterceptor interceptor = new RateLimitingInterceptor(
        new ObjectMapper().findAndRegisterModules(),
        2,
        60
    );

    @Test
    void limitsRequestsPerClientIp() throws Exception {
        assertThat(interceptor.preHandle(request("203.0.113.10"), new MockHttpServletResponse(), new Object()))
            .isTrue();
        assertThat(interceptor.preHandle(request("203.0.113.10"), new MockHttpServletResponse(), new Object()))
            .isTrue();

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request("203.0.113.10"), response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isNotBlank();
        assertThat(response.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }

    @Test
    void tracksDifferentClientIpsSeparately() throws Exception {
        assertThat(interceptor.preHandle(request("203.0.113.20"), new MockHttpServletResponse(), new Object()))
            .isTrue();
        assertThat(interceptor.preHandle(request("203.0.113.20"), new MockHttpServletResponse(), new Object()))
            .isTrue();

        assertThat(interceptor.preHandle(request("203.0.113.21"), new MockHttpServletResponse(), new Object()))
            .isTrue();
    }

    @Test
    void usesFirstForwardedForIp() throws Exception {
        MockHttpServletRequest request = request("198.51.100.1, 198.51.100.2");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
    }

    private MockHttpServletRequest request(String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/resources");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", forwardedFor);
        return request;
    }
}
