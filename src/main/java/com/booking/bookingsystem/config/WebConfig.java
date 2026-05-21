package com.booking.bookingsystem.config;

import com.booking.bookingsystem.interceptor.LoggingInterceptor;
import com.booking.bookingsystem.interceptor.RateLimitingInterceptor;
import com.booking.bookingsystem.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final RateLimitingInterceptor rateLimitingInterceptor;

    public WebConfig(LoggingInterceptor loggingInterceptor, RateLimitingInterceptor rateLimitingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(rateLimitingInterceptor)
            .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    private static class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                && Long.class.equals(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(@NonNull MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      @NonNull NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            Object currentUserId = request == null ? null : request.getAttribute("currentUserId");
            if (currentUserId instanceof Long id) {
                return id;
            }
            return 1L;
        }
    }
}
