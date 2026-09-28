package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCustomerNumberContainingIgnoreCase(String customerNumber);

    List<Customer> findByPhoneContaining(String phone);

    // FIX: Make sure every letter matches this exact name perfectly!
    List<Customer> findByCustomerNumberContainingIgnoreCaseOrPhoneContaining(String customerNumber, String phone);
}
