package fi.haagahelia.carworkshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fi.haagahelia.carworkshop.domain.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/maintenances")
public class MaintenanceController {
    
    @Autowired
    private MaintenanceRepository maintenanceRepository;
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/{id}")
    public String getMaintenanceDetails(@PathVariable Long id, 
                                      Model model, 
                                      Principal principal,
                                      @RequestParam(required = false) Long impersonatedUserId) {
        Optional<Maintenance> maintenanceOpt = maintenanceRepository.findById(id);
        
        if (!maintenanceOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        Maintenance maintenance = maintenanceOpt.get();
        Car car = maintenance.getCar();
        
        // Check if user is the car owner
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
        
        // Handle admin impersonation
        User effectiveUser = user;
        if (isAdmin && impersonatedUserId != null) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            if (impersonatedUserOpt.isPresent()) {
                effectiveUser = impersonatedUserOpt.get();
                model.addAttribute("impersonatedUser", effectiveUser);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
        }
        
        if (!isAdmin && !car.getOwner().getId().equals(user.getId())) {
            return "redirect:/cars/all";
        }
        
        model.addAttribute("maintenance", maintenance);
        model.addAttribute("car", car);
        model.addAttribute("isAdmin", isAdmin);
        
        return "maintenance-details";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditMaintenanceForm(@PathVariable Long id, 
                                        Model model, 
                                        Principal principal,
                                        @RequestParam(required = false) Long impersonatedUserId) {
        Optional<Maintenance> maintenanceOpt = maintenanceRepository.findById(id);
        
        if (!maintenanceOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        Maintenance maintenance = maintenanceOpt.get();
        Car car = maintenance.getCar();
        
        // Check if user is the car owner
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
        
        // Handle admin impersonation
        User effectiveUser = user;
        if (isAdmin && impersonatedUserId != null) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            if (impersonatedUserOpt.isPresent()) {
                effectiveUser = impersonatedUserOpt.get();
                model.addAttribute("impersonatedUser", effectiveUser);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
        }
        
        if (!isAdmin && !car.getOwner().getId().equals(user.getId())) {
            return "redirect:/cars/all";
        }
        
        model.addAttribute("maintenance", maintenance);
        model.addAttribute("car", car);
        model.addAttribute("isAdmin", isAdmin);
        
        return "edit-maintenance";
    }
    
    @PostMapping("/edit/{id}")
    public String updateMaintenance(@PathVariable Long id,
                                  @RequestParam("maintenanceDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maintenanceDate,
                                  @RequestParam(value = "mileage", required = false) Integer mileage,
                                  @RequestParam("description") String description,
                                  @RequestParam(value = "performedBy", required = false) String performedBy,
                                  @RequestParam(value = "cost", required = false) Double cost,
                                  Principal principal,
                                  @RequestParam(required = false) String returnTo,
                                  @RequestParam(required = false) Long impersonatedUserId,
                                  RedirectAttributes redirectAttributes) {
        
        Optional<Maintenance> maintenanceOpt = maintenanceRepository.findById(id);
        
        if (!maintenanceOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        Maintenance maintenance = maintenanceOpt.get();
        Car car = maintenance.getCar();
        Long carId = car.getId();
        
        // Check if user is the car owner
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
        
        if (!isAdmin && !car.getOwner().getId().equals(user.getId())) {
            return "redirect:/cars/all";
        }
        
        // Update maintenance record
        maintenance.setMaintenanceDate(maintenanceDate);
        if (mileage != null) {
            maintenance.setMileage(mileage);
        }
        maintenance.setDescription(description);
        maintenance.setPerformedBy(performedBy);
        maintenance.setCost(cost);
        
        maintenanceRepository.save(maintenance);
        
        redirectAttributes.addFlashAttribute("success", "Maintenance record updated successfully");

        // Determine where to redirect based on returnTo parameter
        if ("cars".equals(returnTo)) {
            if (impersonatedUserId != null) {
                redirectAttributes.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            return "redirect:/cars/all";
        } else {
            if (impersonatedUserId != null) {
                redirectAttributes.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            return "redirect:/cars/record/" + carId;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteMaintenance(@PathVariable Long id, 
                                  Principal principal, 
                                  @RequestParam(required = false) String returnTo,
                                  @RequestParam(required = false) Long impersonatedUserId,
                                  RedirectAttributes redirectAttributes) {
        Optional<Maintenance> maintenanceOpt = maintenanceRepository.findById(id);
        
        if (!maintenanceOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        Maintenance maintenance = maintenanceOpt.get();
        Car car = maintenance.getCar();
        Long carId = car.getId();
        
        // Check if user is the car owner
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
        
        if (!isAdmin && !car.getOwner().getId().equals(user.getId())) {
            return "redirect:/cars/all";
        }
        
        maintenanceRepository.delete(maintenance);
        
        redirectAttributes.addFlashAttribute("success", "Maintenance record deleted successfully");
        
        // Determine where to redirect based on returnTo parameter
        if ("cars".equals(returnTo)) {
            if (impersonatedUserId != null) {
                redirectAttributes.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            return "redirect:/cars/all";
        } else {
            if (impersonatedUserId != null) {
                redirectAttributes.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            return "redirect:/cars/record/" + carId;
        }
    }
} 