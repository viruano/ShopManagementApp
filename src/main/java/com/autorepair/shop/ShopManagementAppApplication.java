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

    // =========================================================
    // ⚡ SaaS INITIAL DATA RECONCILIATION & DEMO SEEDING ENGINE
    // =========================================================
    @Bean
    public CommandLineRunner databaseTenantMigrationBridge(
            CustomerRepository customerRepository,
            org.springframework.data.jpa.repository.JpaRepository<Vehicle, Long> vehicleRepository,
            org.springframework.data.jpa.repository.JpaRepository<WorkOrder, Long> workOrderRepository) {
        return args -> {


            // 🚨 THE CRITICAL DATABASE TRUNCATION LAYER:
            // This clears out all your historical data logs completely across all environments!
            System.out.println("⚠️ [TRUNCATE] Executing full platform dataset wipe down across all tables...");

            // Delete records in reverse order of relationships to prevent foreign key constraint breaks
            workOrderRepository.deleteAll();
            vehicleRepository.deleteAll();
            customerRepository.deleteAll();

            System.out.println("🧼 [TRUNCATE] Success! All database rows have been truncated clean.");


            System.out.println("🔄 [MIGRATION] Checking for historical customer records missing SaaS tenant tokens...");

            // Phase A: Heal any old production rows missing a tenant ID
            List<Customer> allCustomers = customerRepository.findAll();
            long migrationCount = 0;
            for (Customer customer : allCustomers) {
                if (customer.getTenantId() == null || customer.getTenantId().trim().isEmpty()) {
                    customer.setTenantId("xyz-auto");
                    customerRepository.save(customer);
                    migrationCount++;
                }
            }
            if (migrationCount > 0) {
                System.out.println("✅ [MIGRATION] Success! Permanently mapped " + migrationCount + " client rows to 'xyz-auto' workspace core.");
            }

            // 👑 PHASE B: AUTO-SEED THE MAIN STREET DEMO GARAGE WORKSPACE
            // Checks if the Demo Tenant already has data to prevent duplicate seeding loops
            List<Customer> existingDemoClients = customerRepository.findByTenantId("main-st-demo");

            if (existingDemoClients.isEmpty()) {
                System.out.println("🚚 [SEEDER] Main Street Demo Garage workspace is empty. Generating premium sample dataset...");

                // 1. Create Mock Customer Profile
                Customer demoCustomer = new Customer();

                // 👑 THE CRITICAL DATA INTEGRITY SAFEGUARD FIX:
                demoCustomer.setCustomerNumber("CUST-DEMO-991"); // Fulfills the SQL NOT-NULL constraint!

                demoCustomer.setFirstName("Sarah");
                demoCustomer.setLastName("Jenkins");
                demoCustomer.setPhone("(443) 555-8821");
                demoCustomer.setEmail("sarah.demo@example.com");
                demoCustomer.setStreet("412 Main Street");
                demoCustomer.setCity("Annapolis");
                demoCustomer.setState("MD");
                demoCustomer.setZip("21401");
                demoCustomer.setTaxExempt(false);
                demoCustomer.setTenantId("main-st-demo");
                demoCustomer = customerRepository.save(demoCustomer);

                // 2. Create Mock Vehicle Service File
                Vehicle demoVehicle = new Vehicle();
                demoVehicle.setYear("2018");
                demoVehicle.setMake("AUDI");
                demoVehicle.setModel("Q5");
                demoVehicle.setSubModel("Premium Plus");
                demoVehicle.setVin("WA1ANAFYJ9021482");
                demoVehicle.setLicensePlate("DEMO-2026");
                demoVehicle.setEngineSize("2.0T");
                demoVehicle.setDrivetrain("AWD");
                demoVehicle.setCustomer(demoCustomer);
                // If your Vehicle entity already has tenantId column, uncomment the line below:
                // demoVehicle.setTenantId("main-st-demo");
                vehicleRepository.save(demoVehicle);

                // 3. Create Mock Work Order Worksheet
                WorkOrder demoOrder = new WorkOrder();
                demoOrder.setInvoiceNumber("WO-DEMO-01");
                demoOrder.setStatus(WorkOrderStatus.OPENED); // Leaves it active so they can see input cards
                demoOrder.setOdometerIn(64210);
                demoOrder.setOdometerOut(64212);
                demoOrder.setVehicle(demoVehicle);
                demoOrder.setAmountPaid(java.math.BigDecimal.ZERO);
                // If your WorkOrder entity already has tenantId column, uncomment the line below:
                // demoOrder.setTenantId("main-st-demo");

                // 4. Populate Itemized Line Rows directly into the Work Order array ledger
                LineItem laborRow = new LineItem();
                laborRow.setItemType(LineItemType.LABOR);
                laborRow.setDescription("Synthetic Engine Oil & Filter Service (Includes Inspection)");
                laborRow.setQuantity(new java.math.BigDecimal("0.5"));
                laborRow.setRetailPrice(new java.math.BigDecimal("125.00")); // Billed to client
                laborRow.setWholesaleCost(java.math.BigDecimal.ZERO);
                laborRow.setWorkOrder(demoOrder);

                LineItem partRow1 = new LineItem();
                partRow1.setItemType(LineItemType.PART);
                partRow1.setDescription("Mobil 1 Advanced Full Synthetic 5W-30 Motor Oil");
                partRow1.setPartNumber("MOB-5W30-5Q");
                partRow1.setVendor("NAPA Auto Parts");
                partRow1.setQuantity(new java.math.BigDecimal("1.0"));
                partRow1.setWholesaleCost(new java.math.BigDecimal("24.50")); // Shop Paid Cost
                partRow1.setRetailPrice(new java.math.BigDecimal("49.00"));   // 100% Markup applied via matrix rules!
                partRow1.setWorkOrder(demoOrder);

                LineItem partRow2 = new LineItem();
                partRow2.setItemType(LineItemType.PART);
                partRow2.setDescription("Premium Mahle Eco Oil Filter Component");
                partRow2.setPartNumber("MAH-OX388D");
                partRow2.setVendor("AutoZone Commercial");
                partRow2.setQuantity(new java.math.BigDecimal("1.0"));
                partRow2.setWholesaleCost(new java.math.BigDecimal("6.20"));
                partRow2.setRetailPrice(new java.math.BigDecimal("18.60")); // Marked up via low-tier 3.0x scale matrix rules!
                partRow2.setWorkOrder(demoOrder);

                // Bind rows into the ticket reference collection
                demoOrder.setLineItems(java.util.List.of(laborRow, partRow1, partRow2));

                workOrderRepository.save(demoOrder);
                System.out.println("✅ [SEEDER] Demo environment fully initialized! Mock Audi Q5 worksheet created under 'main-st-demo'.");
            } else {
                System.out.println("ℹ️ [SEEDER] Demo environment already contains data logs. Skipping background auto-seed.");
            }
        };
    }

}
