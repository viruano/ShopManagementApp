package com.autorepair.shop;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "line_items")
public class LineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private LineItemType itemType; // PART or LABOR enum discriminator

    // 🚗 AUTOMOTIVE PARTS & PROCUREMENT TRACKING METRICS
    private String partNumber; // Blank for labor; stores SKU/Part number for parts (e.g., NAPA "FIL-1356")
    private String vendor;     // Blank for labor; tracks distributor origin (e.g., "AutoZone", "NAPA")

    @Column(name = "description", nullable = false)
    private String description; // Description text line (e.g., "Premium Front Ceramic Brake Pads")

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE; // Represents parts count (e.g., 2.0) or labor hours (e.g., 1.5)

    @Column(name = "wholesale_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal wholesaleCost = BigDecimal.ZERO; // The price the shop paid the distributor for this specific item

    @Column(name = "retail_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice = BigDecimal.ZERO; // The billed price to the customer per unit

    // =========================================================
    // 🧠 REAL-TIME AUTO WORKSHOP FINANCIAL CALCULATORS
    // =========================================================

    // Calculates the total billed amount to the customer for this specific line item
    public BigDecimal getLineTotal() {
        return retailPrice.multiply(quantity);
    }

    // Calculates the absolute wholesale cost paid out by the shop to stock/buy this row item
    public BigDecimal getLineWholesaleTotal() {
        if (itemType == LineItemType.LABOR) return BigDecimal.ZERO; // Labor rows possess no external wholesale cost
        return wholesaleCost.multiply(quantity);
    }

    // Calculates the exact net dollars remaining in the shop drawer after vendor costs are settled
    public BigDecimal getLineGrossProfit() {
        return getLineTotal().subtract(getLineWholesaleTotal());
    }

    // =========================================================
    // 🧱 CONSTRUCTORS & STANDARD BEAN ENCAPSULATION SETTERS/GETTERS
    // =========================================================
    public LineItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }

    public LineItemType getItemType() { return itemType; }
    public void setItemType(LineItemType itemType) { this.itemType = itemType; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber != null ? partNumber.toUpperCase().trim() : null; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity != null ? quantity : BigDecimal.ONE; }

    public BigDecimal getWholesaleCost() { return wholesaleCost; }
    public void setWholesaleCost(BigDecimal wholesaleCost) { this.wholesaleCost = wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice != null ? retailPrice : BigDecimal.ZERO; }
}
