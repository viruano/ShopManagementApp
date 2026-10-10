package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // Isolate a part directly matching its barcode or SKU string value
    Optional<InventoryItem> findByPartNumber(String partNumber);

    // 🔍 AUTOMOTIVE RADIAL SEARCH ENGINE:
    // Filters through database part numbers, brands, or descriptions simultaneously
    @Query("SELECT i FROM InventoryItem i WHERE " +
            "LOWER(i.partNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.vendor) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<InventoryItem> searchShopInventoryLive(@Param("query") String query);

    // Isolate inventory levels that have dipped below their safety reorder thresholds
    List<InventoryItem> findByQuantityOnHandLessThanEqual(Integer reorderPoint);
}
