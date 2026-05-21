ALTER TABLE slots RENAME TO resources;
ALTER TABLE resources RENAME COLUMN slot_code TO resource_code;
ALTER TABLE resources RENAME COLUMN type TO category;
ALTER TABLE resources ALTER COLUMN category SET DEFAULT 'general';
ALTER TABLE resources DROP COLUMN IF EXISTS start_time;
ALTER TABLE resources DROP COLUMN IF EXISTS end_time;

ALTER TABLE slot_features RENAME TO resource_features;
ALTER TABLE resource_features RENAME COLUMN slot_id TO resource_id;

ALTER TABLE bookings RENAME COLUMN slot_id TO resource_id;

ALTER TABLE resource_features
    RENAME CONSTRAINT fk_slot_features_slot TO fk_resource_features_resource;

ALTER TABLE bookings
    RENAME CONSTRAINT fk_bookings_slot TO fk_bookings_resource;
