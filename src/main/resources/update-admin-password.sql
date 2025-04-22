-- SQL script to update admin password to 'thisisadminpassword'
-- The password is encoded with BCrypt, so we need to use the encoded value
-- This is an example of the encoded value, which will be different each time
-- but will be valid for authentication

UPDATE users 
SET password = '$2a$10$eSBrF6.KJPgj3At4GeFZFem9rjSMf4iPqpsSA/DEF6fZUAZUDYlVC' 
WHERE username = 'admin'; 