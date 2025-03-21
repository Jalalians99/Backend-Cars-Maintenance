package fi.haagahelia.carworkshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.security.Principal;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;

import fi.haagahelia.carworkshop.domain.Car;
import fi.haagahelia.carworkshop.domain.CarRepository;
import fi.haagahelia.carworkshop.domain.User;
import fi.haagahelia.carworkshop.domain.UserRepository;
import fi.haagahelia.carworkshop.domain.OilChange;
import fi.haagahelia.carworkshop.domain.OilChangeRepository;
import fi.haagahelia.carworkshop.domain.Maintenance;
import fi.haagahelia.carworkshop.domain.MaintenanceRepository;

@Controller
public class CarController {
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OilChangeRepository oilChangeRepository;
    
    @Autowired
    private MaintenanceRepository maintenanceRepository;
    
    // Get the effective user (either current user or impersonated user for admin)
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
    
    @GetMapping("/cars")
    public String redirectToCarsAll() {
        return "redirect:/cars/all";
    }
    
    @GetMapping("/cars/all")
    public String userCars(Model model, @AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) Long impersonatedUserId,
                          HttpSession session) {
        
        // Check for impersonated user ID in session if not in request param
        if (impersonatedUserId == null) {
            impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
        }
        
        User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
        
        if (effectiveUser == null) {
            return "redirect:/dashboard";
        }
        
        // Fetch cars using just one method to avoid duplicates
        List<Car> cars = carRepository.findByOwner(effectiveUser);
        
        model.addAttribute("cars", cars);
        
        // Add flag for admin view
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        // If admin is impersonating, also add the user's details
        if (isAdmin && impersonatedUserId != null) {
            model.addAttribute("impersonatedUser", effectiveUser);
            // Pass the ID for subsequent requests
            model.addAttribute("impersonatedUserId", impersonatedUserId);
        }
        
        return "cars";
    }
    
    @GetMapping("/cars/add")
    public String showAddCarForm(Model model, @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) Long impersonatedUserId,
                               HttpSession session) {
        // Check for impersonated user ID in session if not in request param
        if (impersonatedUserId == null) {
            impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
        }
        
        model.addAttribute("carSubTypes", getCarSubTypes());
        
        // Add flag for admin view and impersonated user
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        if (isAdmin && impersonatedUserId != null) {
            User impersonatedUser = userRepository.findById(impersonatedUserId).orElse(null);
            model.addAttribute("impersonatedUser", impersonatedUser);
            // Pass the ID for subsequent requests
            model.addAttribute("impersonatedUserId", impersonatedUserId);
        }
        
        return "add-car";
    }
    
    @PostMapping("/cars/add")
    public String addCar(@RequestParam String make,
                         @RequestParam String model,
                         @RequestParam int year,
                         @RequestParam String licensePlate,
                         @RequestParam String subType,
                         @RequestParam int mileage,
                         @RequestParam(required = false) String lastMaintenance,
                         @RequestParam(required = false) String lastOilChangeDate,
                         @RequestParam(required = false, defaultValue = "0") Integer lastOilChangeMileage,
                         @RequestParam(required = false, defaultValue = "0") Integer oilChangeInterval,
                         @AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) Long impersonatedUserId,
                         RedirectAttributes redirectAttributes) {
        
        User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
        
        if (effectiveUser == null) {
            return "redirect:/dashboard";
        }
        
        // Parse dates safely
        LocalDate maintenanceDate = null;
        if (lastMaintenance != null && !lastMaintenance.isEmpty()) {
            try {
                maintenanceDate = LocalDate.parse(lastMaintenance);
            } catch (Exception e) {
                // If parsing fails, leave as null
            }
        }
        
        LocalDate oilChangeDate = null;
        if (lastOilChangeDate != null && !lastOilChangeDate.isEmpty()) {
            try {
                oilChangeDate = LocalDate.parse(lastOilChangeDate);
            } catch (Exception e) {
                // If parsing fails, leave as null
            }
        }
        
        // Default to 0 if null to prevent NullPointerException
        if (lastOilChangeMileage == null) lastOilChangeMileage = 0;
        if (oilChangeInterval == null) oilChangeInterval = 0;
        
        Car car = new Car(make, model, year, licensePlate, subType, mileage, 
                        maintenanceDate, oilChangeDate, lastOilChangeMileage, 
                        oilChangeInterval, effectiveUser);
        
        carRepository.save(car);
        
        // If admin is impersonating, pass the user ID back
        if (impersonatedUserId != null) {
            redirectAttributes.addFlashAttribute("impersonatedUserId", impersonatedUserId);
        }
        
        return "redirect:/cars/all";
    }
    
    @GetMapping("/cars/edit/{id}")
    public String showEditCarForm(@PathVariable Long id, Model model, 
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
            
            model.addAttribute("car", car);
            model.addAttribute("carSubTypes", getCarSubTypes());
            
            // Add flag for admin view
            model.addAttribute("isAdmin", isAdmin);
            
            // If admin is impersonating, also add the user's details
            if (isAdmin && impersonatedUserId != null) {
                model.addAttribute("impersonatedUser", effectiveUser);
                // Pass the ID for subsequent requests
                model.addAttribute("impersonatedUserId", impersonatedUserId);
            }
            
            return "edit-car";
        } else {
            return "redirect:/cars/all";
        }
    }
    
    @PostMapping("/cars/edit/{id}")
    public String updateCar(@PathVariable Long id,
                           @RequestParam String make,
                           @RequestParam String model,
                           @RequestParam int year,
                           @RequestParam String licensePlate,
                           @RequestParam String subType,
                           @RequestParam int mileage,
                           @RequestParam(required = false) String lastMaintenance,
                           @RequestParam(required = false) String lastOilChangeDate,
                           @RequestParam(required = false, defaultValue = "0") Integer lastOilChangeMileage,
                           @RequestParam(required = false, defaultValue = "0") Integer oilChangeInterval,
                           @AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) Long impersonatedUserId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

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
            
            // Update car details
            car.setMake(make);
            car.setModel(model);
            car.setYear(year);
            car.setLicensePlate(licensePlate);
            car.setSubType(subType);
            car.setMileage(mileage);
            car.setOilChangeInterval(oilChangeInterval);
            
            // Parse and set last maintenance date if provided
            if (lastMaintenance != null && !lastMaintenance.isEmpty()) {
                try {
                    car.setLastMaintenance(LocalDate.parse(lastMaintenance));
                } catch (Exception e) {
                    // If parsing fails, don't update
                }
            }
            
            // Parse and set last oil change date if provided
            if (lastOilChangeDate != null && !lastOilChangeDate.isEmpty()) {
                try {
                    car.setLastOilChangeDate(LocalDate.parse(lastOilChangeDate));
                    car.setLastOilChangeMileage(lastOilChangeMileage);
                } catch (Exception e) {
                    // If parsing fails, don't update
                }
            }
            
            carRepository.save(car);
            
            if (impersonatedUserId != null) {
                redirectAttributes.addAttribute("impersonatedUserId", impersonatedUserId);
                return "redirect:/cars/all?impersonatedUserId=" + impersonatedUserId;
            } else {
                return "redirect:/cars/all";
            }
        } else {
            return "redirect:/cars/all";
        }
    }
    
    @PostMapping("/cars/delete/{id}")
    public String deleteCar(@PathVariable Long id, 
                          @AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) Long impersonatedUserId,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        
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
            
            // Delete the car
            carRepository.deleteById(id);
            
            if (impersonatedUserId != null) {
                redirectAttributes.addAttribute("impersonatedUserId", impersonatedUserId);
                return "redirect:/cars/all?impersonatedUserId=" + impersonatedUserId;
            } else {
                return "redirect:/cars/all";
            }
        }
        
        return "redirect:/cars/all";
    }
    
    @GetMapping("/record/{id}")
    public String showCarRecord(@PathVariable("id") Long id, 
                               @RequestParam(required = false) Long impersonatedUserId,
                               Model model, Principal principal) {
        // Get logged in user
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Check if admin is impersonating a user
        boolean isAdmin = user.getRole().equals("ADMIN");
        User impersonatedUser = null;
        if (impersonatedUserId != null && isAdmin) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            impersonatedUser = impersonatedUserOpt.orElse(null);
            model.addAttribute("impersonatedUser", impersonatedUser);
            model.addAttribute("isAdmin", isAdmin);
        }
        
        // Get the car with the given id
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            
            // Check ownership - either the car belongs to the logged in user or the impersonated user
            if (car.getOwner().getId().equals(user.getId()) || 
                (impersonatedUser != null && car.getOwner().getId().equals(impersonatedUser.getId()))) {
                
                // Get oil changes and maintenance records for this car
                List<OilChange> oilChanges = oilChangeRepository.findByCarIdOrderByChangeDateDesc(car.getId());
                List<Maintenance> maintenanceRecords = maintenanceRepository.findByCarIdOrderByMaintenanceDateDesc(car.getId());
                
                model.addAttribute("car", car);
                model.addAttribute("oilChanges", oilChanges);
                model.addAttribute("maintenanceRecords", maintenanceRecords);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
                
                return "car-record";
            }
        }
        
        // Car not found or not owned by user, redirect to cars list
        return "redirect:/cars";
    }
    
    @GetMapping("/oil-change/{id}")
    public String showAddOilChangeForm(@PathVariable("id") Long id,
                                      @RequestParam(required = false) Long impersonatedUserId,
                                      Model model, Principal principal) {
        // Get logged in user
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Check if admin is impersonating a user
        boolean isAdmin = user.getRole().equals("ADMIN");
        User impersonatedUser = null;
        if (impersonatedUserId != null && isAdmin) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            impersonatedUser = impersonatedUserOpt.orElse(null);
            model.addAttribute("impersonatedUser", impersonatedUser);
            model.addAttribute("isAdmin", isAdmin);
        }
        
        // Get the car with the given id
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            
            // Check ownership - either the car belongs to the logged in user or the impersonated user
            if (car.getOwner().getId().equals(user.getId()) || 
                (impersonatedUser != null && car.getOwner().getId().equals(impersonatedUser.getId()))) {
                
                model.addAttribute("car", car);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
                
                return "add-oil-change";
            }
        }
        
        // Car not found or not owned by user, redirect to cars list
        return "redirect:/cars";
    }
    
    @PostMapping("/oil-change/{id}")
    public String addOilChange(@PathVariable("id") Long id,
                              @RequestParam("changeDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate changeDate,
                              @RequestParam("mileage") int mileage,
                              @RequestParam(value = "oilType", required = false) String oilType,
                              @RequestParam(value = "filterChanged", required = false) Boolean filterChanged,
                              @RequestParam(value = "notes", required = false) String notes,
                              @RequestParam(required = false) Long impersonatedUserId,
                              Principal principal) {
        
        // Get logged in user
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Check if admin is impersonating a user
        boolean isAdmin = user.getRole().equals("ADMIN");
        User impersonatedUser = null;
        if (impersonatedUserId != null && isAdmin) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            impersonatedUser = impersonatedUserOpt.orElse(null);
        }
        
        // Get the car with the given id
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            
            // Check ownership - either the car belongs to the logged in user or the impersonated user
            if (car.getOwner().getId().equals(user.getId()) || 
                (impersonatedUser != null && car.getOwner().getId().equals(impersonatedUser.getId()))) {
                
                // Create and save new oil change record
                OilChange oilChange = new OilChange(changeDate, mileage, oilType, 
                                                  filterChanged != null ? filterChanged : false, 
                                                  notes, car);
                
                oilChangeRepository.save(oilChange);
                
                // Update car's mileage if the new oil change has a higher mileage
                if (mileage > car.getMileage()) {
                    car.setMileage(mileage);
                    carRepository.save(car);
                }
                
                // Redirect to car record page
                if (impersonatedUserId != null) {
                    return "redirect:/cars/record/" + id + "?impersonatedUserId=" + impersonatedUserId;
                } else {
                    return "redirect:/cars/record/" + id;
                }
            }
        }
        
        // Car not found or not owned by user, redirect to cars list
        return "redirect:/cars/all";
    }
    
    @GetMapping("/maintenance/{id}")
    public String showAddMaintenanceForm(@PathVariable("id") Long id,
                                        @RequestParam(required = false) Long impersonatedUserId,
                                        Model model, Principal principal) {
        // Get logged in user
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Check if admin is impersonating a user
        boolean isAdmin = user.getRole().equals("ADMIN");
        User impersonatedUser = null;
        if (impersonatedUserId != null && isAdmin) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            impersonatedUser = impersonatedUserOpt.orElse(null);
            model.addAttribute("impersonatedUser", impersonatedUser);
            model.addAttribute("isAdmin", isAdmin);
        }
        
        // Get the car with the given id
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            
            // Check ownership - either the car belongs to the logged in user or the impersonated user
            if (car.getOwner().getId().equals(user.getId()) || 
                (impersonatedUser != null && car.getOwner().getId().equals(impersonatedUser.getId()))) {
                
                model.addAttribute("car", car);
                model.addAttribute("impersonatedUserId", impersonatedUserId);
                
                return "add-maintenance";
            }
        }
        
        // Car not found or not owned by user, redirect to cars list
        return "redirect:/cars";
    }
    
    @PostMapping("/maintenance/{id}")
    public String addMaintenance(@PathVariable("id") Long id,
                               @RequestParam("maintenanceDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maintenanceDate,
                               @RequestParam(value = "mileage", required = false) Integer mileage,
                               @RequestParam("description") String description,
                               @RequestParam(value = "performedBy", required = false) String performedBy,
                               @RequestParam(value = "cost", required = false) Double cost,
                               @RequestParam(required = false) Long impersonatedUserId,
                               Principal principal) {
        
        // Get logged in user
        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Check if admin is impersonating a user
        boolean isAdmin = user.getRole().equals("ADMIN");
        User impersonatedUser = null;
        if (impersonatedUserId != null && isAdmin) {
            Optional<User> impersonatedUserOpt = userRepository.findById(impersonatedUserId);
            impersonatedUser = impersonatedUserOpt.orElse(null);
        }
        
        // Get the car with the given id
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            
            // Check ownership - either the car belongs to the logged in user or the impersonated user
            if (car.getOwner().getId().equals(user.getId()) || 
                (impersonatedUser != null && car.getOwner().getId().equals(impersonatedUser.getId()))) {
                
                // If mileage is not provided, use car's current mileage
                if (mileage == null) {
                    mileage = car.getMileage();
                }
                
                // Create and save new maintenance record
                Maintenance maintenance = new Maintenance(maintenanceDate, mileage, description, 
                                                        performedBy, cost, car);
                
                maintenanceRepository.save(maintenance);
                
                // Update car's mileage if the new maintenance has a higher mileage
                if (mileage > car.getMileage()) {
                    car.setMileage(mileage);
                    carRepository.save(car);
                }
                
                // Redirect to car record page
                if (impersonatedUserId != null) {
                    return "redirect:/cars/record/" + id + "?impersonatedUserId=" + impersonatedUserId;
                } else {
                    return "redirect:/cars/record/" + id;
                }
            }
        }
        
        // Car not found or not owned by user, redirect to cars list
        return "redirect:/cars/all";
    }
    
    @GetMapping("/cars/search")
    public String searchCars(Model model, 
                           @AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) String searchTerm,
                           @RequestParam(required = false) Long impersonatedUserId,
                           HttpSession session) {
        
        // Check for impersonated user ID in session if not in request param
        if (impersonatedUserId == null) {
            impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
        }
        
        User effectiveUser = getEffectiveUser(userDetails, impersonatedUserId);
        
        if (effectiveUser == null) {
            return "redirect:/dashboard";
        }
        
        List<Car> searchResults = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            try {
                // Try to parse as integer for year search
                int year = Integer.parseInt(searchTerm.trim());
                searchResults = carRepository.findByOwnerAndYear(effectiveUser, year);
            } catch (NumberFormatException e) {
                // Not a number, search across other fields
                String term = searchTerm.trim();
                
                // Check if search contains make and model (format: "Make Model")
                String[] parts = term.split("\\s+", 2);
                if (parts.length > 1) {
                    // Search for make and model
                    searchResults = carRepository.findByOwnerAndMakeContainingIgnoreCaseAndModelContainingIgnoreCase(
                            effectiveUser, parts[0], parts[1]);
                } else {
                    // Search across all text fields
                    searchResults = carRepository.searchByTerm(effectiveUser, term);
                }
            }
        } else {
            // Empty search returns all cars
            searchResults = carRepository.findByOwner(effectiveUser);
        }
        
        model.addAttribute("cars", searchResults);
        model.addAttribute("searchTerm", searchTerm);
        
        // Add flag for admin view
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        // If admin is impersonating, also add the user's details
        if (isAdmin && impersonatedUserId != null) {
            model.addAttribute("impersonatedUser", effectiveUser);
            // Pass the ID for subsequent requests
            model.addAttribute("impersonatedUserId", impersonatedUserId);
        }
        
        return "cars";
    }
    
    private String[] getCarSubTypes() {
        return new String[] {
            "All-terrain SUV", "Convertible", "Coupé", "Hatchback", 
            "MPV", "Racing vehicle", "Sedan", "Station Wagon", "Other"
        };
    }
} 