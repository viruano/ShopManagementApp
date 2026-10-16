package com.autorepair.shop;

import jakarta.persistence.*;

@Entity
@Table(name = "saas_tenants")
public class Tenant {

    @Id
    private String id; // Matches your context tokens like "xyz-auto" or "main-st-demo"

    @Column(nullable = false)
    private String shopName;

    private String address;
    private String phone;
    private boolean isDemoMode = false;

    public Tenant() {}

    public Tenant(String id, String shopName, String address, String phone, boolean isDemoMode) {
        this.id = id;
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.isDemoMode = isDemoMode;
    }

    // =========================================================
    // Core Getters & Setters Matrix Mapping
    // =========================================================
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isDemoMode() { return isDemoMode; }
    public void setDemoMode(boolean demoMode) { this.isDemoMode = demoMode; }
}
