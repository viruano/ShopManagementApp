package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // Allows us to instantly fetch all vehicles belonging to a specific customer account later
    List<Vehicle> findByCustomerId(Long customerId);
}