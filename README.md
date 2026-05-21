# Booking System

Spring Boot booking system API scaffold with resources, bookings, payments, security, validation, caching, and Flyway migrations.

## API Documentation

Swagger UI is available after starting the backend:

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Protected endpoints use JWT bearer authentication. Click `Authorize` in Swagger UI and enter a token returned from `POST /api/v1/auth/login`.

## Admin Access

Public registration creates `CUSTOMER` users only. Admin-only actions, such as creating resources and refunding payments, require an `ADMIN` or `MANAGER` token.

The application bootstraps a default admin account if needed:

- Email: `admin@booking.local`
- Password: `AdminPass123!`

These can be overridden with:

- `booking.admin.email`
- `booking.admin.password`
- `booking.admin.full-name`
