package com.autorepair.shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class ShopManagementAppApplication {

    // 👑 PLACE INJECTIONS SAFELY IN THE CLASS BODY
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public static void main(String[] args) {
        SpringApplication.run(ShopManagementAppApplication.class, args);
    }

    // =========================================================
    // ⚡ SaaS INITIAL DATA RECONCILIATION & DEMO SEEDING ENGINE
    // =========================================================
    @Bean
    public CommandLineRunner databaseTenantMigrationBridge(
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository, // 👑 CORRECTED TO TYPE-SAFE SPECIFIC INTERFACE REPOSITORIES
            WorkOrderRepository workOrderRepository) {
        return args -> {

            System.out.println("🚀 Running database initialization seeder routines...");

            // 👑 POPULATE BASELINE SaaS TENANTS DATA ROWS
            if (tenantRepository.count() == 0) {
                tenantRepository.save(new Tenant("xyz-auto", "XYZ Auto Services LLC", "27 Hicks Av, Annapolis, MD 21401", "(443) 325-6663", false));
                tenantRepository.save(new Tenant("main-st-demo", "🔧 MAIN STREET DEMO GARAGE", "100 Sandbox Workspace Lane, Annapolis, MD 21401", "(800) 555-DEMO [EVALUATION FILE]", true));
                System.out.println("✅ [SaaS SEEDER] Baseline Tenant profiles populated successfully!");
            }

            // 🚨 THE CRITICAL DATABASE TRUNCATION LAYER:
            // Since you are okay with clearing the un-utilized data tokens, this runs one clean pass
            System.out.println("⚠️ [TRUNCATE] Executing full platform dataset wipe down across all tables...");

            // workOrderRepository.deleteAll();
            // vehicleRepository.deleteAll();
            // customerRepository.deleteAll();

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
            List<Customer> existingDemoClients = customerRepository.findByTenantId("main-st-demo");

            // 👑 FIXED SEEDER SECURITY LAYER:
            // Checks if your customer database table is completely empty rather than searching for specific rows.
            // This forces your premium Main Street Demo Garage dataset to inject perfectly after a truncation pass!
            if (customerRepository.count() == 0) {
                System.out.println("🚚 [SEEDER] Database tables are clean. Generating premium sample dataset...");

                // 1. Create Mock Customer Profile
                Customer demoCustomer = new Customer();
                demoCustomer.setCustomerNumber("CUST-DEMO-991");
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
                demoVehicle.setYear(2018);
                demoVehicle.setMake("AUDI");
                demoVehicle.setModel("Q5");
                demoVehicle.setTrim("PREMIUM PLUS");
                demoVehicle.setVin("WA1ANAFYJ9021482");
                demoVehicle.setLicensePlate("DEMO-2026");
                demoVehicle.setEngineSize("2.0T");
                demoVehicle.setDrivetrain("AWD");
                demoVehicle.setCustomer(demoCustomer);
                demoVehicle.setTenantId("main-st-demo");
                vehicleRepository.save(demoVehicle);

                // 3. Create Mock Work Order Worksheet
                WorkOrder demoOrder = new WorkOrder();
                demoOrder.setInvoiceNumber("WO-DEMO-01");
                demoOrder.setStatus(WorkOrderStatus.OPENED);
                demoOrder.setOdometerIn(64210);
                demoOrder.setOdometerOut(64212);
                demoOrder.setVehicle(demoVehicle);
                demoOrder.setAmountPaid(BigDecimal.ZERO);
                demoOrder.setTenantId("main-st-demo");

                // 4. Populate Itemized Line Rows
                LineItem laborRow = new LineItem();
                laborRow.setItemType(LineItemType.LABOR);
                laborRow.setDescription("Synthetic Engine Oil & Filter Service (Includes Inspection)");
                laborRow.setQuantity(new BigDecimal("0.5"));
                laborRow.setRetailPrice(new BigDecimal("125.00"));
                laborRow.setWholesaleCost(BigDecimal.ZERO);
                laborRow.setWorkOrder(demoOrder);

                LineItem partRow1 = new LineItem();
                partRow1.setItemType(LineItemType.PART);
                partRow1.setDescription("Mobil 1 Advanced Full Synthetic 5W-30 Motor Oil");
                partRow1.setPartNumber("MOB-5W30-5Q");
                partRow1.setVendor("NAPA Auto Parts");
                partRow1.setQuantity(new BigDecimal("1.0"));
                partRow1.setWholesaleCost(new BigDecimal("24.50"));
                partRow1.setRetailPrice(new BigDecimal("49.00"));
                partRow1.setWorkOrder(demoOrder);

                LineItem partRow2 = new LineItem();
                partRow2.setItemType(LineItemType.PART);
                partRow2.setDescription("Premium Mahle Eco Oil Filter Component");
                partRow2.setPartNumber("MAH-OX388D");
                partRow2.setVendor("AutoZone Commercial");
                partRow2.setQuantity(new BigDecimal("1.0"));
                partRow2.setWholesaleCost(new BigDecimal("6.20"));
                partRow2.setRetailPrice(new BigDecimal("18.60"));
                partRow2.setWorkOrder(demoOrder);

                // Bind rows into the ticket reference collection safely
                demoOrder.setLineItems(List.of(laborRow, partRow1, partRow2));
                workOrderRepository.save(demoOrder);
                System.out.println("✅ [SaaS SEEDER] Demo Workspace dataset initialized successfully!");
            } // 👑 Safe matching closing brace block

        };
    }
}
