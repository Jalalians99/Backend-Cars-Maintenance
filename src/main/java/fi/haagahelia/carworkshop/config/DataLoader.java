package fi.haagahelia.carworkshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import fi.haagahelia.carworkshop.domain.User;
import fi.haagahelia.carworkshop.domain.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("admin")) {
            // Create admin user
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("thisisadminpassword"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@carworkshop.com");
            adminUser.setPhoneNumber("1234567890");
            adminUser.setRole("ROLE_ADMIN");
            
            userRepository.save(adminUser);
            
            System.out.println("Admin user created successfully.");
        }
    }
} 