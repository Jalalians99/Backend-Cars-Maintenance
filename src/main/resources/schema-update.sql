-- Rename columns in the cars table from "last" to "first"
ALTER TABLE IF EXISTS cars RENAME COLUMN last_maintenance TO first_maintenance;
ALTER TABLE IF EXISTS cars RENAME COLUMN last_oil_change_date TO first_oil_change;
ALTER TABLE IF EXISTS cars RENAME COLUMN last_oil_change_mileage TO mileage_at_first_oil_change;

-- Update user_id column to owner_id for better clarity
ALTER TABLE IF EXISTS cars RENAME COLUMN user_id TO owner_id;