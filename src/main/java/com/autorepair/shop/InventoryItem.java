package com.autorepair.shop;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_number", unique = true, nullable = false)
    private String partNumber; // The distributor or manufacturer SKU code (e.g., NAPA "FIL-1356")

    @Column(nullable = false)
    private String name; // Consumer-facing title (e.g., "Premium Front Ceramic Brake Pads")

    private String vendor; // Wholesaler name (e.g., "AutoZone", "NAPA", "Local Ford Dealer")

    @Column(name = "wholesale_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal wholesaleCost = BigDecimal.ZERO; // What the shop pays the vendor for the part

    @Column(name = "retail_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice = BigDecimal.ZERO; // What you bill the customer on the final invoice

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0; // The active physical count sitting on your shop's shelves

    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint = 3; // Triggers a dashboard flag when shelf inventory dips below this safety threshold

    @Column(name = "is_tracked", nullable = false)
    private Boolean isTracked = true; // true = tracked warehouse stock (oil/wipers); false = temporary custom ordered parts

    // =========================================================
    // 🧠 CORE AUTOMOTIVE MARACTION ALGORITHMS
    // =========================================================
    public BigDecimal getGrossProfitDollarAmount() {
        return retailPrice.subtract(wholesaleCost);
    }

    public BigDecimal getMarkupPercentage() {
        if (wholesaleCost.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getGrossProfitDollarAmount()
                .divide(wholesaleCost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // =========================================================
    // 🧱 CLASS CONSTRUCTORS & ENCAPSULATION SETTERS/GETTERS
    // =========================================================
    public InventoryItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber != null ? partNumber.toUpperCase().trim() : null; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public BigDecimal getWholesaleCost() { return wholesaleCost; }
    public void setWholesaleCost(BigDecimal wholesaleCost) { this.wholesaleCost = wholesaleCost; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public Integer getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public Boolean getIsTracked() { return isTracked; }
    public void setIsTracked(Boolean isTracked) { this.isTracked = isTracked; }
}
