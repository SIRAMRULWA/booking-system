package com.booking.bookingsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI bookingOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Booking System API")
                .version("v1")
                .description("REST API documentation for a resource-based booking system")
                .contact(new Contact().name("Booking System Team")))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuthScheme()))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private SecurityScheme bearerAuthScheme() {
        return new SecurityScheme()
            .name(SECURITY_SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");
    }
}
