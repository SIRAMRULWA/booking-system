CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    quantity INTEGER,
    total_price DOUBLE PRECISION,
    notes VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version INTEGER,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_slot FOREIGN KEY (slot_id) REFERENCES slots (id)
);
