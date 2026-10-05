package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ⚡ THE MASTER SHOP SEARCH: Scans across Account IDs, Phone Numbers, First Names, OR Last Names automatically!
    List<Customer> findByCustomerNumberContainingIgnoreCaseOrPhoneContainingOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String customerNumber, String phone, String firstName, String lastName);
}
