package com.autorepair.shop;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                // 👑 COMPOSITE TENANT UNIQUE PERIMETER:
                // Allows the exact same VIN to exist concurrently across different shop environments!
                @UniqueConstraint(columnNames = {"vin", "tenant_id"})
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👑 FIXED: Removed 'unique = true' so uniqueness is bounded strictly to the table metadata pairs
    @Column(nullable = false)
    private String vin;

    // 👑 UPDATED PROPERTY TYPE FROM String TO int NATIVELY
    @Column(name = "vehicle_year")
    private int year;

    private String make;
    private String model;
    private String licensePlate;

    private String trim;
    private String engineSize;
    private String engineCode;
    private String drivetrain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // 📊 ODOMETER ENGINE TRACKING METRICS
    private Integer odometerIn;
    private Integer odometerOut;

    // THE CORRECTED OPERATIONAL REPAIR ORDERS HISTORY LINK:
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkOrder> workOrders = new ArrayList<>();

    // Default Constructor
    public Vehicle() {}

    // =========================================================
    // 🖨️ DEEP COPY CONSTRUCTOR FOR OWNERSHIP TRANSFERS
    // =========================================================
    public Vehicle(Vehicle original, Customer newOwner) {
        // Leave the 'id' field null so Hibernate automatically generates a new database row
        this.vin = original.getVin();
        this.year = original.getYear();
        this.make = original.getMake();
        this.model = original.getModel();
        this.licensePlate = original.getLicensePlate();
        this.trim = original.getTrim();
        this.engineSize = original.getEngineSize();
        this.engineCode = original.getEngineCode();
        this.drivetrain = original.getDrivetrain();
        this.tenantId = original.getTenantId();

        // 🤝 Assign the vehicle directly to the new buyer
        this.customer = newOwner;

        // Initialize clean odometer metrics for the new owner's track
        this.odometerIn = original.getOdometerIn();
        this.odometerOut = 0;
    }

    // =========================================================
    // 🏢 SaaS MULTI-TENANT ISOLATION COLUMN PROPERTY (VEHICLE)
    // =========================================================
    @jakarta.persistence.Column(name = "tenant_id", nullable = true)
    private String tenantId;

    public String getTenantId() {
        // Defensive checking assigns old existing null database rows to your core shop tenant automatically
        if (this.tenantId == null || this.tenantId.trim().isEmpty()) {
            return "xyz-auto";
        }
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId != null ? tenantId.trim() : null;
    }

    // ==========================================
    // Getters and Setters
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin != null ? vin.toUpperCase() : null; }

    // 👑 UPDATED GETTER/SETTER TO INT TYPE MATCHES
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getTrim() { return this.trim; }
    public void setTrim(String trim) { this.trim = trim != null ? trim.trim().toUpperCase() : null; }

    public String getEngineSize() { return engineSize; }
    public void setEngineSize(String engineSize) { this.engineSize = engineSize; }

    public String getEngineCode() { return engineCode; }
    public void setEngineCode(String engineCode) { this.engineCode = engineCode; }

    public String getDrivetrain() { return drivetrain; }
    public void setDrivetrain(String drivetrain) { this.drivetrain = drivetrain; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Integer getOdometerIn() { return odometerIn; }
    public void setOdometerIn(Integer odometerIn) { this.odometerIn = odometerIn; }

    public Integer getOdometerOut() { return odometerOut; }
    public void setOdometerOut(Integer odometerOut) { this.odometerOut = odometerOut; }

    public List<WorkOrder> getWorkOrders() { return workOrders; }
    public void setWorkOrders(List<WorkOrder> workOrders) { this.workOrders = workOrders; }
}
