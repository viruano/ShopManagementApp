package com.autorepair.shop;

import jakarta.persistence.*;
import java.math.BigDecimal;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "line_items")
public class LineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private LineItemType itemType; // PART or LABOR

    @Column(nullable = false)
    private String description;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice; // What the shop paid the parts distributor

    @Column(name = "retail_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal retailPrice; // What the shop charges the customer

    @Column(nullable = false)
    private Double quantity; // Number of parts OR flat-rate billable hours (e.g., 1.5 hours)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    // Default Constructor (Required by JPA)
    public LineItem() {}

    // Convenience Constructor — Updated for Work Order Architecture
    public LineItem(LineItemType itemType, String description, BigDecimal costPrice, BigDecimal retailPrice, Double quantity, WorkOrder workOrder) {
        this.itemType = itemType;
        this.description = description;
        this.costPrice = costPrice;
        this.retailPrice = retailPrice;
        this.quantity = quantity;
        this.workOrder = workOrder; // ⚡ BINDING FIXED
    }

    // Custom Helper: Instantly calculates gross profit for this line item
    public BigDecimal getLineTotal() {
        return retailPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Custom Helper: Instantly calculates the shop's net profit margin dollars
    public BigDecimal getProfitMargin() {
        if (costPrice == null) return BigDecimal.ZERO;
        return getLineTotal().subtract(costPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    // ==========================================
    // Getters and Setters
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LineItemType getItemType() { return itemType; }
    public void setItemType(LineItemType itemType) { this.itemType = itemType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }
}
