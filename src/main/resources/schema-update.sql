-- Rename columns in the cars table from "last" to "first"
ALTER TABLE cars CHANGE COLUMN last_maintenance first_maintenance DATE;
ALTER TABLE cars CHANGE COLUMN last_oil_change_date first_oil_change DATE;
ALTER TABLE cars CHANGE COLUMN last_oil_change_mileage mileage_at_first_oil_change INT;

-- Update user_id column to owner_id for better clarity
ALTER TABLE cars CHANGE COLUMN user_id owner_id BIGINT NOT NULL;