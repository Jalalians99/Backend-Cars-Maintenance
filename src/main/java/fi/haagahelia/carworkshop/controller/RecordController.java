package fi.haagahelia.carworkshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fi.haagahelia.carworkshop.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
public class RecordController {
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OilChangeRepository oilChangeRepository;
    
    @Autowired
    private MaintenanceRepository maintenanceRepository;
    
    // Helper method to get the effective user (reusing from CarController)
    private User getEffectiveUser(UserDetails userDetails, Long impersonatedUserId) {
        // Check if this is an admin impersonating another user
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        if (isAdmin && impersonatedUserId != null) {
            // Admin is impersonating another user
            return userRepository.findById(impersonatedUserId).orElse(null);
        } else {
            // Normal user or admin not impersonating
            return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        }
    }
    
    // Show car records (both maintenance and oil changes)
    @GetMapping("/cars/record/{id}")
    @Transactional
    public String showCarRecord(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) Long impersonatedUserId,
                              HttpSession session) {
        
        // Check for impersonated user ID in session if not in request param
        if (impersonatedUserId == null) {
            impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
        }
        
        Optional<Car> optionalCar = carRepository.findById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
            
            if (effectiveUser == null) {
                return "redirect:/dashboard";
            }
            
            // Check if admin or car owner
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            if (!isAdmin && !car.getOwner().getId().equals(effectiveUser.getId())) {
                return "redirect:/cars/all";
            }
            
            // Get oil changes and maintenance records
            List<OilChange> oilChanges = oilChangeRepository.findByCarIdOrderByChangeDateDesc(car.getId());
            List<Maintenance> maintenanceRecords = maintenanceRepository.findByCarIdOrderByMaintenanceDateDesc(car.getId());
            
            model.addAttribute("car", car);
            model.addAttribute("oilChanges", oilChanges);
            model.addAttribute("maintenanceRecords", maintenanceRecords);
            
            // Add flags for admin view
            model.addAttribute("isAdmin", isAdmin);
            
            // If admin is impersonating, also add the user's details
            if (isAdmin && impersonatedUserId != null) {
                model.addAttribute("impersonatedUser", effectiveUser);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            
            return "car-record";
        } else {
            return "redirect:/cars/all";
        }
    }
    
    // Show form to add oil change
    @GetMapping("/cars/oil-change/{id}")
    @Transactional
    public String showAddOilChangeForm(@PathVariable Long id, Model model,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam(required = false) Long impersonatedUserId) {
        
        Optional<Car> optionalCar = carRepository.findById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
            
            if (effectiveUser == null) {
                return "redirect:/dashboard";
            }
            
            // Check if admin or car owner
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            if (!isAdmin && !car.getOwner().getId().equals(effectiveUser.getId())) {
                return "redirect:/cars/all";
            }
            
            model.addAttribute("car", car);
            model.addAttribute("oilChange", new OilChange());
            
            // Add flag for admin view
            model.addAttribute("isAdmin", isAdmin);
            
            // If admin is impersonating, also add the user's details
            if (isAdmin && impersonatedUserId != null) {
                model.addAttribute("impersonatedUser", effectiveUser);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            
            return "oil-change";
        } else {
            return "redirect:/cars/all";
        }
    }
    
    // Process oil change form submission
    @PostMapping("/cars/oil-change/{id}")
    @Transactional
    public String addOilChange(@PathVariable Long id,
                             @RequestParam String changeDate,
                             @RequestParam int mileage,
                             @RequestParam(required = false) String oilType,
                             @RequestParam(required = false, defaultValue = "false") Boolean filterChanged,
                             @RequestParam(required = false) String notes,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false) Long impersonatedUserId,
                             RedirectAttributes redirectAttributes) {
        
        Optional<Car> optionalCar = carRepository.findById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
            
            if (effectiveUser == null) {
                return "redirect:/dashboard";
            }
            
            // Check if admin or car owner
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            if (!isAdmin && !car.getOwner().getId().equals(effectiveUser.getId())) {
                return "redirect:/cars/all";
            }
            
            // Parse date
            LocalDate parsedDate = null;
            if (changeDate != null && !changeDate.isEmpty()) {
                try {
                    parsedDate = LocalDate.parse(changeDate);
                } catch (Exception e) {
                    // If parsing fails, use current date
                    parsedDate = LocalDate.now();
                }
            } else {
                parsedDate = LocalDate.now();
            }
            
            // Update the car's current mileage if the new mileage is higher
            if (mileage > car.getMileage()) {
                car.setMileage(mileage);
                carRepository.save(car);
            }
            
            // Create and save the oil change record
            OilChange oilChange = new OilChange(parsedDate, mileage, oilType, filterChanged, notes, car);
            oilChangeRepository.save(oilChange);
            
            // If admin is impersonating, pass the user ID back
            if (impersonatedUserId != null) {
                redirectAttributes.addFlashAttribute("impersonatedUserId", impersonatedUserId);
            }
            
            return "redirect:/cars/record/" + id;
        } else {
            return "redirect:/cars/all";
        }
    }
    
    // Show form to add maintenance
    @GetMapping("/cars/maintenance/{id}")
    @Transactional
    public String showAddMaintenanceForm(@PathVariable Long id, Model model,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam(required = false) Long impersonatedUserId) {
        
        Optional<Car> optionalCar = carRepository.findById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
            
            if (effectiveUser == null) {
                return "redirect:/dashboard";
            }
            
            // Check if admin or car owner
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            if (!isAdmin && !car.getOwner().getId().equals(effectiveUser.getId())) {
                return "redirect:/cars/all";
            }
            
            model.addAttribute("car", car);
            model.addAttribute("maintenance", new Maintenance());
            
            // Add flag for admin view
            model.addAttribute("isAdmin", isAdmin);
            
            // If admin is impersonating, also add the user's details
            if (isAdmin && impersonatedUserId != null) {
                model.addAttribute("impersonatedUser", effectiveUser);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            
            return "maintenance";
        } else {
            return "redirect:/cars/all";
        }
    }
    
    // Process maintenance form submission
    @PostMapping("/cars/maintenance/{id}")
    @Transactional
    public String addMaintenance(@PathVariable Long id,
                               @RequestParam String maintenanceDate,
                               @RequestParam(required = false) Integer mileage,
                               @RequestParam String description,
                               @RequestParam(required = false) String performedBy,
                               @RequestParam(required = false) Double cost,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) Long impersonatedUserId,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Car> optionalCar = carRepository.findById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
            
            if (effectiveUser == null) {
                return "redirect:/dashboard";
            }
            
            // Check if admin or car owner
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            if (!isAdmin && !car.getOwner().getId().equals(effectiveUser.getId())) {
                return "redirect:/cars/all";
            }
            
            // Parse date
            LocalDate parsedDate = null;
            if (maintenanceDate != null && !maintenanceDate.isEmpty()) {
                try {
                    parsedDate = LocalDate.parse(maintenanceDate);
                } catch (Exception e) {
                    // If parsing fails, use current date
                    parsedDate = LocalDate.now();
                }
            } else {
                parsedDate = LocalDate.now();
            }
            
            // If mileage is not provided, use the car's current mileage
            if (mileage == null) {
                mileage = car.getMileage();
            } else if (mileage > car.getMileage()) {
                // Update the car's current mileage if the new mileage is higher
                car.setMileage(mileage);
                carRepository.save(car);
            }
            
            // Create and save the maintenance record
            Maintenance maintenance = new Maintenance(parsedDate, mileage, description, performedBy, cost, car);
            maintenanceRepository.save(maintenance);
            
            // If admin is impersonating, pass the user ID back
            if (impersonatedUserId != null) {
                redirectAttributes.addFlashAttribute("impersonatedUserId", impersonatedUserId);
            }
            
            return "redirect:/cars/record/" + id;
        } else {
            return "redirect:/cars/all";
        }
    }
} 