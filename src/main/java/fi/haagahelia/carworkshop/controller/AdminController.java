package fi.haagahelia.carworkshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import fi.haagahelia.carworkshop.domain.Car;
import fi.haagahelia.carworkshop.domain.CarRepository;
import fi.haagahelia.carworkshop.domain.User;
import fi.haagahelia.carworkshop.domain.UserRepository;
import fi.haagahelia.carworkshop.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("")
    public String adminDashboard(Model model, HttpSession session) {
        // Check if admin is impersonating someone
        Long impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
        if (impersonatedUserId != null) {
            Optional<User> impersonatedUser = userRepository.findById(impersonatedUserId);
            if (impersonatedUser.isPresent()) {
                model.addAttribute("impersonatedUser", impersonatedUser.get());
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
        }
        
        model.addAttribute("isAdmin", true);
        model.addAttribute("hideUserNav", true);
        long userCount = userRepository.count();
        long carCount = carRepository.count();
        model.addAttribute("userCount", userCount);
        model.addAttribute("carCount", carCount);
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String listUsers(Model model, @RequestParam(required = false) String searchTerm) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("hideUserNav", true);
        
        List<User> userResults = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Search for users by term
            userResults = userRepository.searchNonAdminByTerm(searchTerm.trim());
        } else {
            // Get all users
            Iterable<User> allUsers = userRepository.findAll();
            
            // Filter out admin users for impersonation
            for (User user : allUsers) {
                // Only add non-admin users to the list for impersonation
                if (!user.getRole().equals("ROLE_ADMIN")) {
                    userResults.add(user);
                }
            }
        }
        
        model.addAttribute("users", userResults);
        model.addAttribute("searchTerm", searchTerm);
        return "admin/users";
    }
    
    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("hideUserNav", true);
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        List<Car> userCars = carRepository.findByOwner(user);

        model.addAttribute("user", user);
        model.addAttribute("cars", userCars);
        return "admin/user-details";
    }
    
    @GetMapping("/users/{id}/search")
    public String searchUserCars(@PathVariable Long id, 
                               @RequestParam(required = false) String searchTerm,
                               Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("hideUserNav", true);
        
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        List<Car> searchResults = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            try {
                // Try to parse as integer for year search
                int year = Integer.parseInt(searchTerm.trim());
                searchResults = carRepository.findByOwnerAndYear(user, year);
            } catch (NumberFormatException e) {
                // Not a number, search across other fields
                String term = searchTerm.trim();
                
                // Check if search contains make and model (format: "Make Model")
                String[] parts = term.split("\\s+", 2);
                if (parts.length > 1) {
                    // Search for make and model
                    searchResults = carRepository.findByOwnerAndMakeContainingIgnoreCaseAndModelContainingIgnoreCase(
                            user, parts[0], parts[1]);
                } else {
                    // Search across all text fields
                    searchResults = carRepository.searchByTerm(user, term);
                }
            }
        } else {
            // Empty search returns all cars
            searchResults = carRepository.findByOwner(user);
        }

        model.addAttribute("user", user);
        model.addAttribute("cars", searchResults);
        model.addAttribute("searchTerm", searchTerm);
        
        return "admin/user-details";
    }
    
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        // Don't allow deleting the admin user
        Optional<User> userToDelete = userRepository.findById(id);
        
        if (userToDelete.isPresent() && !userToDelete.get().getRole().equals("ROLE_ADMIN")) {
            User user = userToDelete.get();
            
            // First delete all cars owned by this user
            List<Car> userCars = carRepository.findByOwner(user);
            carRepository.deleteAll(userCars);
            
            // Then delete the user
            userRepository.deleteById(id);
        }
        
        return "redirect:/admin/users";
    }
    
    @GetMapping("/impersonate/{id}")
    public String impersonateUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails adminUser, 
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (optionalUser.isPresent()) {
            // Store impersonated user's ID in session for persistence across requests
            session.setAttribute("impersonatedUserId", id);
            
            // Also add to flash attributes for first redirect
            redirectAttributes.addFlashAttribute("impersonatedUserId", id);
            redirectAttributes.addAttribute("impersonatedUserId", id);
            
            // Redirect to the user's cars page
            return "redirect:/cars/all?impersonatedUserId=" + id;
        } else {
            return "redirect:/admin/users";
        }
    }
} 