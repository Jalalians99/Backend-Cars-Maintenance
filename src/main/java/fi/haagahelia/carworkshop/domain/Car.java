package fi.haagahelia.carworkshop.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String make;
    
    @Column(nullable = false)
    private String model;
    
    @Column(nullable = false)
    private int year;
    
    @Column(name = "license_plate", nullable = false)
    private String licensePlate;
    
    @Column(name = "sub_type", nullable = false)
    private String subType;
    
    @Column(nullable = false)
    private int mileage;
    
    @Column(name = "first_maintenance")
    private LocalDate firstMaintenance;
    
    @Column(name = "first_oil_change")
    private LocalDate firstOilChangeDate;
    
    @Column(name = "mileage_at_first_oil_change")
    private Integer mileageAtFirstOilChange;
    
    @Column(name = "oil_change_interval")
    private Integer oilChangeInterval;
    
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<OilChange> oilChanges;
    
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Maintenance> maintenanceRecords;
    
    public Car() {}
    
    public Car(String make, String model, int year, String licensePlate, 
              String subType, int mileage, LocalDate firstMaintenance, 
              LocalDate firstOilChangeDate, Integer mileageAtFirstOilChange, 
              Integer oilChangeInterval, User owner) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.subType = subType;
        this.mileage = mileage;
        this.firstMaintenance = firstMaintenance;
        this.firstOilChangeDate = firstOilChangeDate;
        this.mileageAtFirstOilChange = mileageAtFirstOilChange;
        this.oilChangeInterval = oilChangeInterval;
        this.owner = owner;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMake() {
        return make;
    }
    
    public void setMake(String make) {
        this.make = make;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public String getSubType() {
        return subType;
    }
    
    public void setSubType(String subType) {
        this.subType = subType;
    }
    
    public int getMileage() {
        return mileage;
    }
    
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
    
    public LocalDate getFirstMaintenance() {
        return firstMaintenance;
    }
    
    public void setFirstMaintenance(LocalDate firstMaintenance) {
        this.firstMaintenance = firstMaintenance;
    }
    
    public LocalDate getFirstOilChangeDate() {
        return firstOilChangeDate;
    }
    
    public void setFirstOilChangeDate(LocalDate firstOilChangeDate) {
        this.firstOilChangeDate = firstOilChangeDate;
    }
    
    public Integer getMileageAtFirstOilChange() {
        return mileageAtFirstOilChange;
    }
    
    public void setMileageAtFirstOilChange(Integer mileageAtFirstOilChange) {
        this.mileageAtFirstOilChange = mileageAtFirstOilChange;
    }
    
    public Integer getOilChangeInterval() {
        return oilChangeInterval;
    }
    
    public void setOilChangeInterval(Integer oilChangeInterval) {
        this.oilChangeInterval = oilChangeInterval;
    }
    
    public User getOwner() {
        return owner;
    }
    
    public void setOwner(User owner) {
        this.owner = owner;
    }
    
    // For backward compatibility with existing code
    public LocalDate getLastMaintenance() {
        return firstMaintenance;
    }
    
    public void setLastMaintenance(LocalDate maintenance) {
        this.firstMaintenance = maintenance;
    }
    
    public LocalDate getLastOilChangeDate() {
        return firstOilChangeDate;
    }
    
    public void setLastOilChangeDate(LocalDate oilChangeDate) {
        this.firstOilChangeDate = oilChangeDate;
    }
    
    public Integer getLastOilChangeMileage() {
        return mileageAtFirstOilChange;
    }
    
    public void setLastOilChangeMileage(Integer mileage) {
        this.mileageAtFirstOilChange = mileage;
    }
} 