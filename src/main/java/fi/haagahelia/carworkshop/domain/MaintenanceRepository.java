package fi.haagahelia.carworkshop.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    
    // Find all maintenance records for a specific car
    List<Maintenance> findByCarId(Long carId);
    
    // Find all maintenance records for cars owned by a specific user
    List<Maintenance> findByCarOwnerId(Long ownerId);
    
    // Find maintenance records sorted by date (most recent first)
    List<Maintenance> findByCarIdOrderByMaintenanceDateDesc(Long carId);
    
    // Delete all maintenance records for a specific car
    void deleteByCarId(Long carId);
} 