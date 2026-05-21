# Best UI for Booking System API

This UI is aligned with backend DTO/entities:
- `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserResponse`
- `ResourceRequest`, `ResourceResponse`
- `BookingRequest`, `BookingResponse` (including nested `payment`)
- `PaymentRequest`, `PaymentResponse`
- Enums: `PaymentMethod`

## Run

Open `index.html` directly in your browser, or serve the folder:

```powershell
cd best-ui
npx --yes serve -l 5500 .
```

Then open `http://localhost:5500`.

## Notes

- Default API base URL is `http://localhost:8080`.
- JWT token from login/register is stored in `localStorage` and sent as `Authorization: Bearer <token>`.
- Some actions require role permissions:
  - Create Resource: `ADMIN` or `MANAGER`
  - Start/Complete Booking: `ADMIN` or `MANAGER`
  - Refund: `ADMIN` or `MANAGER`
  - User bookings endpoint may require `CUSTOMER`.
