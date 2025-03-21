package fi.haagahelia.carworkshop.domain;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepository extends CrudRepository<Car, Long> {
    List<Car> findByOwnerId(Long ownerId);
    
    List<Car> findByOwner(User owner);
    
    // Search methods
    List<Car> findByOwnerAndMakeContainingIgnoreCase(User owner, String make);
    
    List<Car> findByOwnerAndMakeContainingIgnoreCaseAndModelContainingIgnoreCase(User owner, String make, String model);
    
    List<Car> findByOwnerAndLicensePlateContainingIgnoreCase(User owner, String licensePlate);
    
    List<Car> findByOwnerAndSubTypeContainingIgnoreCase(User owner, String subType);
    
    List<Car> findByOwnerAndYear(User owner, int year);
    
    // Custom query for searching across multiple fields
    @Query("SELECT c FROM Car c WHERE c.owner = :owner AND " +
           "(LOWER(c.make) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.model) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.licensePlate) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.subType) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Car> searchByTerm(@Param("owner") User owner, @Param("term") String term);
} 