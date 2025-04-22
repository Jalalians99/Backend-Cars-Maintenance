-- Rename columns in the cars table from "last" to "first"
ALTER TABLE IF EXISTS cars RENAME COLUMN last_maintenance TO first_maintenance;
ALTER TABLE IF EXISTS cars RENAME COLUMN last_oil_change_date TO first_oil_change;
ALTER TABLE IF EXISTS cars RENAME COLUMN last_oil_change_mileage TO mileage_at_first_oil_change;

-- Update user_id column to owner_id for better clarity
ALTER TABLE IF EXISTS cars RENAME COLUMN user_id TO owner_id;

-- Update admin password to "thisisadminpassword"
-- Using a BCrypt encoded password
UPDATE users SET password = '$2a$10$g32qG5JBMFIZ9ww/93M/GeRj9/kfT4E.gZW4P6ZE4Z3ZZQpMns5Hi' WHERE username = 'admin';