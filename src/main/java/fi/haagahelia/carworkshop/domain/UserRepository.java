package fi.haagahelia.carworkshop.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Search methods for admin
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    List<User> findByEmailContainingIgnoreCase(String email);
    
    // Combined search method
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<User> searchByTerm(@Param("term") String term);
    
    // Search method with role exclusion
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
           "u.role != 'ROLE_ADMIN'")
    List<User> searchNonAdminByTerm(@Param("term") String term);
} 