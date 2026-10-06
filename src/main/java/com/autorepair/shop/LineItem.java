package com.autorepair.shop;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "line_items")
public class LineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LineItemType itemType; // PART or LABOR

    private String description;
    private BigDecimal costPrice = BigDecimal.ZERO;  // Wholesale purchase outlay cost
    private BigDecimal retailPrice = BigDecimal.ZERO; // Customer billing rate
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    public LineItem() {}

    public LineItem(LineItemType itemType, String description, BigDecimal costPrice, BigDecimal retailPrice, Double quantity, WorkOrder workOrder) {
        this.itemType = itemType;
        this.description = description;
        this.costPrice = costPrice;
        this.retailPrice = retailPrice;
        this.quantity = quantity;
        this.workOrder = workOrder;
    }

    public BigDecimal getLineTotal() {
        return retailPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Standard Getters & Setters mapping variables...
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
