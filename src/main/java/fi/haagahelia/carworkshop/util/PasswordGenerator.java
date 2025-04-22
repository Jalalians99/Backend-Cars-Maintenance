package fi.haagahelia.carworkshop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "thisisadminpassword";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Password: " + rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
        System.out.println();
        
        System.out.println("SQL to update admin password:");
        System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE username = 'admin';");
    }
} 