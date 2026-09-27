package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LineItemRepository extends JpaRepository<LineItem, Long> {

    // Custom database search query to find all parts/labor items billed to a specific vehicle chassis
    List<LineItem> findByVehicleId(Long vehicleId);
}
