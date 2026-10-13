package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 🔒 THE MULTI-TENANT REPOSITORY SAFE GUARD MATCHERS:
    List<Vehicle> findByTenantId(String tenantId);

    Optional<Vehicle> findByIdAndTenantId(Long id, String tenantId);

    // 🔒 REVISED MULTI-TENANT TENANT-SAFE VEHICLE SEARCH ENGINE
    // Confines license plate and VIN scans strictly to the active shop floor instance
    @Query("SELECT v FROM Vehicle v WHERE v.tenantId = :tenantId AND (" +
            "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.vin) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Vehicle> searchTenantVehicles(
            @Param("keyword") String keyword,
            @Param("tenantId") String tenantId);
}
