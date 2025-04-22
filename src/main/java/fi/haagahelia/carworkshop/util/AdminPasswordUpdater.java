package fi.haagahelia.carworkshop.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import fi.haagahelia.carworkshop.domain.User;
import fi.haagahelia.carworkshop.domain.UserRepository;
import java.util.Optional;

@Component
@Profile("password-update")
public class AdminPasswordUpdater implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Find the admin user
        Optional<User> adminUserOpt = userRepository.findByUsername("admin");
        
        if (adminUserOpt.isPresent()) {
            User adminUser = adminUserOpt.get();
            // Update the password to the new one
            adminUser.setPassword(passwordEncoder.encode("thisisadminpassword"));
            userRepository.save(adminUser);
            
            System.out.println("Admin password updated successfully to 'thisisadminpassword'.");
            System.exit(0); // Exit after updating password
        } else {
            System.out.println("Admin user not found!");
        }
    }
} 