package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaborGuideRepository extends JpaRepository<LaborGuide, Long> {
    // Inherits standard database capabilities automatically
}
