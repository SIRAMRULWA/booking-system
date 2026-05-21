CREATE TABLE IF NOT EXISTS slots (
    id BIGSERIAL PRIMARY KEY,
    slot_code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    capacity INTEGER,
    price DOUBLE PRECISION,
    description VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    version INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS slot_features (
    slot_id BIGINT NOT NULL,
    feature VARCHAR(255),
    CONSTRAINT fk_slot_features_slot FOREIGN KEY (slot_id) REFERENCES slots (id)
);
