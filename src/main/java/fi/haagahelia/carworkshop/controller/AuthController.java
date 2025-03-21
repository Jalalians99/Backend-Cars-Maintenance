package fi.haagahelia.carworkshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import fi.haagahelia.carworkshop.domain.User;
import fi.haagahelia.carworkshop.service.UserService;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }
    
    @PostMapping("/signup")
    public String registerUser(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              @RequestParam String phoneNumber,
                              @RequestParam String dateOfBirth,
                              Model model) {
        
        // Check if username or email is already taken
        if (userService.isUsernameTaken(username)) {
            model.addAttribute("error", "Username is already taken");
            return "signup";
        }
        
        if (userService.isEmailTaken(email)) {
            model.addAttribute("error", "Email is already registered");
            return "signup";
        }
        
        // Create and save the new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Will be encoded in service
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setDateOfBirth(LocalDate.parse(dateOfBirth));
        
        userService.saveUser(user);
        
        return "redirect:/login?registered";
    }
    
    @GetMapping("/reset-password")
    public String showResetForm() {
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    public String processResetRequest(@RequestParam String emailOrPhone, Model model) {
        // Try to find user by email
        Optional<User> userByEmail = userService.findByEmail(emailOrPhone);
        
        // If not found by email, try phone number
        Optional<User> user = userByEmail.isPresent() ? userByEmail : userService.findByPhoneNumber(emailOrPhone);
        
        if (user.isPresent()) {
            // In a real application, send password reset link to email
            // For this example, we'll just show a success message
            model.addAttribute("message", "Password reset link has been sent to your email address");
            return "reset-success";
        } else {
            model.addAttribute("error", "No account found with that email or phone number");
            return "reset-password";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
} 