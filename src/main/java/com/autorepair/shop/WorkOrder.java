package com.autorepair.shop;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber; // Auto-generated e.g., WO-1001
    private LocalDateTime dateOpened;
    private LocalDateTime dateClosed;

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status = WorkOrderStatus.OPENED;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private BigDecimal amountPaid = BigDecimal.ZERO;
    private Integer odometerIn;
    private Integer odometerOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems = new ArrayList<>();

    public WorkOrder() {
        this.dateOpened = LocalDateTime.now();
    }

    // Calculates the absolute total wholesale parts cost outlays tied to this single repair ticket
    public BigDecimal getPartsWholesaleTotalSum() {
        return lineItems.stream()
                .filter(item -> item.getItemType() == LineItemType.PART)
                .map(LineItem::getLineWholesaleTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Calculates the net gross profit dollar sum cleared on this specific invoice record
    public BigDecimal getWorkOrderGrossProfit() {
        return getTotalDue().subtract(getPartsWholesaleTotalSum());
    }

    // 💰 FINANCIAL CALCULATORS (Executed live on dashboard loads)
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

    // =========================================================
    // 🧮 AGGREGATE FINANCIAL BALANCE ENGINE MATRICES
    // =========================================================
    public BigDecimal getTotalDue() {
        BigDecimal partsTotal = getPartsSubtotal();
        BigDecimal laborTotal = getLaborSubtotal();

        // 🔒 SAFETY TOGGLE ENGINE: Automatically strips sales tax if the profile is flagged as a B2B Fleet Account
        BigDecimal taxRate = new BigDecimal("0.06"); // Standard 6% base tax rule sample
        if (this.vehicle != null && this.vehicle.getCustomer() != null && this.vehicle.getCustomer().isTaxExempt()) {
            taxRate = BigDecimal.ZERO;
        }

        BigDecimal partsTax = partsTotal.multiply(taxRate);
        return partsTotal.add(laborTotal).add(partsTax).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getRemainingBalance() {
        return getTotalDue().subtract(amountPaid);
    }

    // =========================================================
    // 📈 GRAND METRIC REVENUE ACCUMULATORS
    // =========================================================

    /**
     * Aggregates total net profit dollars across all part and material items on the ticket.
     */
    public BigDecimal getGrandProfitDollars() {
        if (this.lineItems == null || this.lineItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return this.lineItems.stream()
                .map(LineItem::getMarginProfitDollars)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =========================================================
    // 📈 AUTOMATED AUTOMOTIVE MATRIX MULTIPLIER CALCULATOR
    // =========================================================

    /**
     * Applies a sliding scale parts multiplier matrix to maximize retail profitability.
     * Tier 1: $0.00 to $10.00 ➔ 3.0x Multiplier (300% Gross Markup)
     * Tier 2: $10.01 to $50.00 ➔ 2.0x Multiplier (200% Gross Markup)
     * Tier 3: $50.01 to $100.00 ➔ 1.6x Multiplier (160% Gross Markup)
     * Tier 4: $100.01 to $300.00 ➔ 1.45x Multiplier (145% Gross Markup)
     * Tier 5: Over $300.01 ➔ 1.3x Multiplier (130% Gross Markup)
     * [Shopmonkey auto repair app standard pricing rule configuration blueprint]
     */
    public static BigDecimal calculateMatrixRetailPrice(BigDecimal wholesaleCost) {
        if (wholesaleCost == null || wholesaleCost.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal multiplier;

        if (wholesaleCost.compareTo(new BigDecimal("10.00")) <= 0) {
            multiplier = new BigDecimal("3.00");
        } else if (wholesaleCost.compareTo(new BigDecimal("50.00")) <= 0) {
            multiplier = new BigDecimal("2.00");
        } else if (wholesaleCost.compareTo(new BigDecimal("10.000")) <= 0) {
            multiplier = new BigDecimal("1.60");
        } else if (wholesaleCost.compareTo(new BigDecimal("300.00")) <= 0) {
            multiplier = new BigDecimal("1.45");
        } else {
            multiplier = new BigDecimal("1.30");
        }

        return wholesaleCost.multiply(multiplier).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // Standard Getters & Setters mapping parameters completely...
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
