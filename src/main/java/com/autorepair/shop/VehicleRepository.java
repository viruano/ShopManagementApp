package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Dynamic Lookup Engine: Scans license plates or VIN chassis codes ignoring case matching parameters
    List<Vehicle> findByLicensePlateContainingIgnoreCaseOrVinContainingIgnoreCase(String licensePlate, String vin);
}
