package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 🔒 Standard Tenant Matchers
    List<Customer> findByTenantId(String tenantId);
    Optional<Customer> findByIdAndTenantId(Long id, String tenantId);

    // 🔒 REVISED MULTI-TENANT TENANT-SAFE SEARCH ENGINE
    // Combines your cross-column scanner but confines parameters to the active shop floor instance
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND (" +
            "LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phone LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Customer> searchTenantCustomers(
            @Param("keyword") String keyword,
            @Param("tenantId") String tenantId);
}
