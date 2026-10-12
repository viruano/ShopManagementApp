package com.autorepair.shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class ShopManagementAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopManagementAppApplication.class, args);
    }

    // ⚡ SaaS INITIAL DATA RECONCILIATION ENGINE
    // Scans database rows on startup and updates any historical rows missing a tenant ID
    @Bean
    public CommandLineRunner databaseTenantMigrationBridge(CustomerRepository customerRepository) {
        return args -> {
            System.out.println("🔄 [MIGRATION] Checking for historical customer records missing SaaS tenant tokens...");

            // Look up all customers currently sitting in your tables
            List<Customer> allCustomers = customerRepository.findAll();
            long migrationCount = 0;

            for (Customer customer : allCustomers) {
                // Check the raw property field directly
                if (customer.getTenantId().equals("xyz-auto") && !customer.getTenantId().equals(customer.getFirstName())) {
                    // If the database column is null, our updated getter returns "xyz-auto".
                    // We force-save it back down so the actual database row writes the text string permanently!
                    customer.setTenantId("xyz-auto");
                    customerRepository.save(customer);
                    migrationCount++;
                }
            }

            if (migrationCount > 0) {
                System.out.println("✅ [MIGRATION] Success! Permanently mapped " + migrationCount + " client rows to 'xyz-auto' workspace core.");
            } else {
                System.out.println("ℹ️ [MIGRATION] Database schema is fully optimized. All client rows carry valid tenant identities.");
            }
        };
    }
}
