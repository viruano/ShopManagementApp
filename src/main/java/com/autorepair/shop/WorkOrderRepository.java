package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    // ⚡ FIND OPEN TICKETS: Used to populate the primary shop floor view by default
    List<WorkOrder> findByStatus(WorkOrderStatus status);

    // 🔍 EXACT INVOICE MATCH: Find a specific document by its number (e.g., WO-1001)
    Optional<WorkOrder> findByInvoiceNumberIgnoreCase(String invoiceNumber);

    // 🚗 FIND BY VEHICLE: Pulls the complete lifecycle history for a specific car profile
    List<WorkOrder> findByVehicleId(Long vehicleId);

    // 👥 FIND BY CUSTOMER ID: Pulls all work orders belonging to a customer's primary key
    List<WorkOrder> findByVehicleCustomerId(Long customerId);

    // 🆔 FIND BY CUSTOMER NUMBER: Pulls all invoices using the public account ID string (e.g., CUST-XXXX)
    List<WorkOrder> findByVehicleCustomerCustomerNumberIgnoreCase(String customerNumber);

    // 🔗 COMBINED MASTER SEARCH ENGINE QUERY: Scans across Invoice #, License Plate, or Owner Last Name
    @Query("SELECT w FROM WorkOrder w WHERE " +
            "LOWER(w.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(w.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(w.vehicle.customer.lastName) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<WorkOrder> searchOrders(@Param("q") String query);
}
