package com.booking.bookingsystem.interceptor;

import com.booking.bookingsystem.dto.response.ErrorResponse;
import com.booking.bookingsystem.filter.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final String UNKNOWN_IP = "unknown";

    private final ObjectMapper objectMapper;
    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Cache<String, ClientRateLimit> clientLimits;

    public RateLimitingInterceptor(ObjectMapper objectMapper,
                                   @Value("${booking.api.rate-limit:100}") int maxRequestsPerWindow,
                                   @Value("${booking.api.rate-limit-window-seconds:60}") long windowSeconds) {
        this.objectMapper = objectMapper;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = Duration.ofSeconds(windowSeconds);
        this.clientLimits = Caffeine.newBuilder()
            .expireAfterAccess(this.window.multipliedBy(2))
            .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = resolveClientIp(request);
        ClientRateLimit limit = clientLimits.get(clientIp, ignored -> new ClientRateLimit(Instant.now()));

        if (!limit.tryAcquire(maxRequestsPerWindow, window)) {
            writeRateLimitResponse(request, response, limit.retryAfterSeconds(window));
            return false;
        }

        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String firstIp = forwardedFor.split(",")[0].trim();
            if (!firstIp.isBlank() && !UNKNOWN_IP.equalsIgnoreCase(firstIp)) {
                return firstIp;
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank() && !UNKNOWN_IP.equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletRequest request,
                                        HttpServletResponse response,
                                        long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Too Many Requests")
            .message("Rate limit exceeded. Try again later.")
            .code("RATE_LIMIT_EXCEEDED")
            .path(request.getRequestURI())
            .requestId(resolveRequestId(request))
            .build();

        objectMapper.writeValue(response.getWriter(), body);
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_HEADER);
        if (requestId instanceof String value && !value.isBlank()) {
            return value;
        }
        return request.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
    }

    private static final class ClientRateLimit {
        private Instant windowStartedAt;
        private int requestCount;

        private ClientRateLimit(Instant windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }

        private synchronized boolean tryAcquire(int maxRequestsPerWindow, Duration window) {
            Instant now = Instant.now();
            if (!now.isBefore(windowStartedAt.plus(window))) {
                windowStartedAt = now;
                requestCount = 0;
            }

            if (requestCount >= maxRequestsPerWindow) {
                return false;
            }

            requestCount++;
            return true;
        }

        private synchronized long retryAfterSeconds(Duration window) {
            long seconds = Duration.between(Instant.now(), windowStartedAt.plus(window)).toSeconds();
            return Math.max(1L, seconds);
        }
    }
}
