package com.autorepair.shop;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String vin;
    // private String year;
    // Escape the protected 'year' keyword for local H2 database compatibility
    @Column(name = "vehicle_year")
    private String year;
    private String make;
    private String model;
    private String licensePlate;

    // --- Added Parts Ordering Attributes ---
    private String subModel;     // e.g., "EX-L"
    private String engineSize;   // e.g., "V6 3.5L"
    private String engineCode;   // e.g., "J35Z8" (Engine Designator)
    private String drivetrain;   // e.g., "FWD"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Default Constructor
    public Vehicle() {}

    // ==========================================
    // Getters and Setters
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin != null ? vin.toUpperCase() : null; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getSubModel() { return subModel; }
    public void setSubModel(String subModel) { this.subModel = subModel; }
    public String getEngineSize() { return engineSize; }
    public void setEngineSize(String engineSize) { this.engineSize = engineSize; }
    public String getEngineCode() { return engineCode; }
    public void setEngineCode(String engineCode) { this.engineCode = engineCode; }
    public String getDrivetrain() { return drivetrain; }
    public void setDrivetrain(String drivetrain) { this.drivetrain = drivetrain; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}