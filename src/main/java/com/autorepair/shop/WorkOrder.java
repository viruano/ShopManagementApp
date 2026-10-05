package com.autorepair.shop;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber; // e.g., INV-1001
    private LocalDateTime dateOpened;
    private LocalDateTime dateClosed;

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status = WorkOrderStatus.OPENED;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private BigDecimal amountPaid = BigDecimal.ZERO;

    private Integer odometerIn;
    private Integer odometerOut;

    @ManyToOne(fetch = FetchType.LAZY) //  THE COMPLIANT CORRECTION ANCHOR
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems = new ArrayList<>();

    public WorkOrder() {
        this.dateOpened = LocalDateTime.now();
    }

    // Helper calculation methods for real-time dashboard analytics
    public BigDecimal getPartsSubtotal() {
        return lineItems.stream()
                .filter(item -> item.getItemType() == LineItemType.PART)
                .map(LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getLaborSubtotal() {
        return lineItems.stream()
                .filter(item -> item.getItemType() == LineItemType.LABOR)
                .map(LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDue() {
        return getPartsSubtotal().add(getLaborSubtotal());
    }

    public BigDecimal getRemainingBalance() {
        return getTotalDue().subtract(amountPaid);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDateTime getDateOpened() { return dateOpened; }
    public void setDateOpened(LocalDateTime dateOpened) { this.dateOpened = dateOpened; }
    public LocalDateTime getDateClosed() { return dateClosed; }
    public void setDateClosed(LocalDateTime dateClosed) { this.dateClosed = dateClosed; }
    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public Integer getOdometerIn() { return odometerIn; }
    public void setOdometerIn(Integer odometerIn) { this.odometerIn = odometerIn; }
    public Integer getOdometerOut() { return odometerOut; }
    public void setOdometerOut(Integer odometerOut) { this.odometerOut = odometerOut; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public List<LineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<LineItem> lineItems) { this.lineItems = lineItems; }
}
