package fi.haagahelia.carworkshop.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface OilChangeRepository extends JpaRepository<OilChange, Long> {
    List<OilChange> findByCar(Car car);
    List<OilChange> findByCarId(Long carId);
    List<OilChange> findByCarOrderByChangeDateDesc(Car car);
    List<OilChange> findByCarIdOrderByChangeDateDesc(Long carId);
} 