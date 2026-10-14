package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    // 🔒 THE SaaS REPOSITORY ISOLATION LEVEL SAFEGUARDS
    List<WorkOrder> findByTenantId(String tenantId);
    List<WorkOrder> findByStatusAndTenantId(WorkOrderStatus status, String tenantId);
    Optional<WorkOrder> findByIdAndTenantId(Long id, String tenantId);
    List<WorkOrder> findByVehicleId(Long vehicleId); // Kept for standalone internal mapping lookups

    // 🚨 REMOVED UNPROTECTED SEARCH ENGINE METHODS
    // 🔒 REVISED MULTI-TENANT TENANT-SAFE FUZZY SEARCH ENGINE
    // Scans Invoice, License Plate, or Customer Last Name but restricts results strictly to the active workspace
    @Query("SELECT w FROM WorkOrder w WHERE w.tenantId = :tenantId AND (" +
            "LOWER(w.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(w.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(w.vehicle.customer.lastName) LIKE LOWER(CONCAT('%', :q, '%'))" +
            ")")
    List<WorkOrder> searchTenantOrders(@Param("q") String query, @Param("tenantId") String tenantId);
}
