package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Custom database search query to look up a customer by their unique Account Number
    Optional<Customer> findByCustomerNumber(String customerNumber);
}