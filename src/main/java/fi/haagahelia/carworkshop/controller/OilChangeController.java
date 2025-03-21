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
@RequestMapping("/oilchanges")
public class OilChangeController {
    
    @Autowired
    private OilChangeRepository oilChangeRepository;
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/{id}")
    public String getOilChangeDetails(@PathVariable Long id, 
                                    Model model, 
                                    Principal principal,
                                    @RequestParam(required = false) Long impersonatedUserId) {
        Optional<OilChange> oilChangeOpt = oilChangeRepository.findById(id);
        
        if (!oilChangeOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        OilChange oilChange = oilChangeOpt.get();
        Car car = oilChange.getCar();
        
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
        
        model.addAttribute("oilChange", oilChange);
        model.addAttribute("car", car);
        model.addAttribute("isAdmin", isAdmin);
        
        return "oil-change-details";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditOilChangeForm(@PathVariable Long id, 
                                      Model model, 
                                      Principal principal,
                                      @RequestParam(required = false) Long impersonatedUserId) {
        Optional<OilChange> oilChangeOpt = oilChangeRepository.findById(id);
        
        if (!oilChangeOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        OilChange oilChange = oilChangeOpt.get();
        Car car = oilChange.getCar();
        
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
        
        model.addAttribute("oilChange", oilChange);
        model.addAttribute("car", car);
        model.addAttribute("isAdmin", isAdmin);
        
        return "edit-oil-change";
    }
    
    @PostMapping("/edit/{id}")
    public String updateOilChange(@PathVariable Long id,
                                @RequestParam("changeDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate changeDate,
                                @RequestParam("mileage") int mileage,
                                @RequestParam(value = "oilType", required = false) String oilType,
                                @RequestParam(value = "filterChanged", required = false) Boolean filterChanged,
                                @RequestParam(value = "notes", required = false) String notes,
                                Principal principal,
                                @RequestParam(required = false) String returnTo,
                                @RequestParam(required = false) Long impersonatedUserId,
                                RedirectAttributes redirectAttributes) {
        
        Optional<OilChange> oilChangeOpt = oilChangeRepository.findById(id);
        
        if (!oilChangeOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        OilChange oilChange = oilChangeOpt.get();
        Car car = oilChange.getCar();
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
        
        // Update oil change properties
        oilChange.setChangeDate(changeDate);
        oilChange.setMileage(mileage);
        oilChange.setOilType(oilType);
        oilChange.setFilterChanged(filterChanged != null ? filterChanged : false);
        oilChange.setNotes(notes);
        
        oilChangeRepository.save(oilChange);
        
        redirectAttributes.addFlashAttribute("success", "Oil change record updated successfully");
        
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
    public String deleteOilChange(@PathVariable Long id, 
                                Principal principal, 
                                @RequestParam(required = false) String returnTo,
                                @RequestParam(required = false) Long impersonatedUserId,
                                RedirectAttributes redirectAttributes) {
        Optional<OilChange> oilChangeOpt = oilChangeRepository.findById(id);
        
        if (!oilChangeOpt.isPresent()) {
            return "redirect:/dashboard";
        }
        
        OilChange oilChange = oilChangeOpt.get();
        Car car = oilChange.getCar();
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
        
        oilChangeRepository.delete(oilChange);
        
        redirectAttributes.addFlashAttribute("success", "Oil change record deleted successfully");
        
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