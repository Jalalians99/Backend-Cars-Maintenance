package fi.haagahelia.carworkshop.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "oil_changes")
public class OilChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;
    
    @Column(name = "mileage", nullable = false)
    private int mileage;
    
    @Column(name = "oil_type")
    private String oilType;
    
    @Column(name = "filter_changed")
    private Boolean filterChanged;
    
    @Column(name = "notes")
    private String notes;
    
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
    
    // Constructors
    public OilChange() {
    }
    
    public OilChange(LocalDate changeDate, int mileage, String oilType, Boolean filterChanged, String notes, Car car) {
        this.changeDate = changeDate;
        this.mileage = mileage;
        this.oilType = oilType;
        this.filterChanged = filterChanged;
        this.notes = notes;
        this.car = car;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(LocalDate changeDate) {
        this.changeDate = changeDate;
    }
    
    public int getMileage() {
        return mileage;
    }
    
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
    
    public String getOilType() {
        return oilType;
    }
    
    public void setOilType(String oilType) {
        this.oilType = oilType;
    }
    
    public Boolean getFilterChanged() {
        return filterChanged;
    }
    
    public void setFilterChanged(Boolean filterChanged) {
        this.filterChanged = filterChanged;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Car getCar() {
        return car;
    }
    
    public void setCar(Car car) {
        this.car = car;
    }
    
    @Override
    public String toString() {
        return "OilChange [id=" + id + ", changeDate=" + changeDate + ", mileage=" + mileage 
                + ", oilType=" + oilType + ", filterChanged=" + filterChanged + "]";
    }
} 