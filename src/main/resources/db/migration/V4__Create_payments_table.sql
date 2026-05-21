CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    transaction_id VARCHAR(100) UNIQUE,
    amount DOUBLE PRECISION,
    method VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP,
    payment_gateway_response VARCHAR(1000),
    created_at TIMESTAMP,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
);
