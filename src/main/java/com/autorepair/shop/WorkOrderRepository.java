package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findByStatus(WorkOrderStatus status);
    List<WorkOrder> findByVehicleId(Long vehicleId);

    // ⚡ FUZZY SEARCH INDEX ENGINE: Scans Invoice, License Plate, or Customer Last Name
    @Query("SELECT w FROM WorkOrder w WHERE " +
            "LOWER(w.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(w.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(w.vehicle.customer.lastName) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<WorkOrder> searchOrders(@Param("q") String query);
}
