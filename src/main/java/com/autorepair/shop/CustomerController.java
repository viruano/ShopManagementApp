package com.autorepair.shop;

// 👑 ADD THIS CRITICAL IMPORT STATEMENT:
import org.springframework.beans.factory.annotation.Value;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    // =========================================================
    // 🌐 MASTER CORE ROUTER: MANAGES WORKSPACE STATE ENGINE
    // =========================================================
    @GetMapping("/")
    public String viewDashboard(
            @RequestParam(value = "tab", required = false, defaultValue = "customers") String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "vehicleSearch", required = false) String vehicleSearch,
            @RequestParam(value = "focusedWorkOrderId", required = false) Long focusedWorkOrderId,
            @RequestParam(value = "editItemId", required = false) Long editItemId,
            @RequestParam(value = "editCustomerId", required = false) Long editCustomerId,
            @RequestParam(value = "editVehicleId", required = false) Long editVehicleId,
            Model model) {

        // 🔒 SaaS MULTI-TENANT IDENTITY CHECK
        // Extract the unique logged-in shop identifier from the active request thread
        String activeTenant = TenantContext.getCurrentTenant();
        if (activeTenant == null) {
            activeTenant = "xyz-auto"; // Safe fallback developer tracker token
        }

        // Retain the active browser panel selection tab state across redirects
        model.addAttribute("activeTab", tab);
        model.addAttribute("editCustomerId", editCustomerId);
        model.addAttribute("editVehicleId", editVehicleId);
        model.addAttribute("editItemId", editItemId);

        // Populate base form bindings for modal/panel intakes
        if (!model.containsAttribute("newCustomer")) model.addAttribute("newCustomer", new Customer());
        if (!model.containsAttribute("newVehicle")) model.addAttribute("newVehicle", new Vehicle());
        if (!model.containsAttribute("newLineItem")) model.addAttribute("newLineItem", new LineItem());

        // 🔒 TENANT-ISOLATED CUSTOMER DATA ENGINE
        // Strictly loads dropdown and list records belonging to the current tenant
        List<Customer> tenantCustomers = customerRepository.findByTenantId(activeTenant);
        model.addAttribute("customers", tenantCustomers);

        // 🚗 Global Fallbacks for vehicle records until column migrations are complete
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        model.addAttribute("vehicles", allVehicles);

        // 👥 PANEL 1 LAYER: CUSTOMERS MANAGEMENT CHECKSETS (TENANT SECURED)
        if ("customers".equals(tab)) {
            List<Customer> filteredCustomers;
            if (search != null && !search.trim().isEmpty()) {
                // Execute our custom fiewall-secure multi-column keyword query mapping
                filteredCustomers = customerRepository.searchTenantCustomers(search.trim(), activeTenant);
            } else {
                filteredCustomers = tenantCustomers;
            }
            model.addAttribute("customers", filteredCustomers);
            model.addAttribute("currentSearch", search);
        }

        // 🚗 PANEL 2 LAYER: VEHICLES FLEET CHECKSETS (Global Mapping)
        if ("vehicles".equals(tab)) {
            List<Vehicle> filteredVehicles = (vehicleSearch != null && !vehicleSearch.trim().isEmpty()) ?
                    vehicleRepository.findByLicensePlateContainingIgnoreCaseOrVinContainingIgnoreCase(
                            vehicleSearch.trim(), vehicleSearch.trim()) : allVehicles;
            model.addAttribute("vehicles", filteredVehicles);
            model.addAttribute("currentVehicleSearch", vehicleSearch);
        }

        // 📋 PANEL 3 LAYER: INVOICING WORK ORDERS ARCHITECTURE (Global Mapping)
        List<WorkOrder> visibleOrders;
        boolean showAll = model.containsAttribute("showAllOrders") && (boolean) model.getAttribute("showAllOrders");
        model.addAttribute("showAllOrders", showAll);

        if ("orders".equals(tab) && search != null && !search.trim().isEmpty()) {
            visibleOrders = workOrderRepository.searchOrders(search.trim());
            model.addAttribute("currentSearch", search);
        } else if (showAll) {
            visibleOrders = workOrderRepository.findAll();
        } else {
            visibleOrders = workOrderRepository.findByStatus(WorkOrderStatus.OPENED);
        }
        model.addAttribute("workOrders", visibleOrders);

        // Isolate totals and line rows if a specific invoice is selected
        if (focusedWorkOrderId != null) {
            workOrderRepository.findById(focusedWorkOrderId).ifPresent(order -> {
                model.addAttribute("focusedWorkOrder", order);
                model.addAttribute("focusedWorkOrderId", focusedWorkOrderId);
                model.addAttribute("lineItems", order.getLineItems());

                // Aggregate financial totals live for this specific ticket scope
                model.addAttribute("totalParts", order.getPartsSubtotal());
                model.addAttribute("totalLabor", order.getLaborSubtotal());
                model.addAttribute("totalRevenue", order.getTotalDue());
                model.addAttribute("totalProfit", order.getGrandProfitDollars());
                model.addAttribute("amountPaid", order.getAmountPaid());
                model.addAttribute("remainingBalance", order.getRemainingBalance());
            });
        } else {
            // Mix global ledger averages if no specific invoice is focused
            BigDecimal globalRev = visibleOrders.stream().map(WorkOrder::getTotalDue).reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalParts", BigDecimal.ZERO);
            model.addAttribute("totalLabor", BigDecimal.ZERO);
            model.addAttribute("totalRevenue", globalRev);
            model.addAttribute("amountPaid", BigDecimal.ZERO);
            model.addAttribute("remainingBalance", globalRev);
        }

        // Mock Labor Matrix Catalog data mapping
        model.addAttribute("laborCatalog", List.of(
                new LaborGuide("BRAKES", "Front Brake Pads & Rotors Replacement", 2.0),
                new LaborGuide("ENGINE", "Synthetic Engine Oil & Filter Service", 0.5),
                new LaborGuide("SUSPENSION", "Four-Wheel Alignment Alignment Service", 1.5),
                new LaborGuide("DIAGNOSTIC", "Electrical System Scanning Fault Check", 1.0)
        ));

        return "index";
    }

    // =========================================================
    // 🏎️ FIXED LIVE EXTERNAL DATA ENGINE USING STRING BUFFER PARSING
    // =========================================================
    private void decodeVinLive(Vehicle vehicle, String vin) {
        if (vin == null || vin.trim().length() < 10) return;

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String apiUrl = "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValues/" + vin.trim() + "?format=json";

            String jsonRaw = restTemplate.getForObject(apiUrl, String.class);

            if (jsonRaw != null && !jsonRaw.trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonRaw);

                if (root != null && root.has("Results")) {
                    com.fasterxml.jackson.databind.JsonNode results = root.get("Results");

                    if (results.isArray() && results.size() > 0) {
                        com.fasterxml.jackson.databind.JsonNode vehicleData = results.get(0);

                        String year = vehicleData.path("ModelYear").asText("");
                        String make = vehicleData.path("Make").asText("");
                        String model = vehicleData.path("Model").asText("");
                        String trim = vehicleData.path("Trim").asText("");
                        String engine = vehicleData.path("DisplacementL").asText("");
                        String drive = vehicleData.path("DriveType").asText("");

                        if (!year.isEmpty() && !year.equalsIgnoreCase("null")) vehicle.setYear(year.trim());
                        if (!make.isEmpty() && !make.equalsIgnoreCase("null")) vehicle.setMake(make.trim().toUpperCase());
                        if (!model.isEmpty() && !model.equalsIgnoreCase("null")) vehicle.setModel(model.trim().toUpperCase());
                        if (!trim.isEmpty() && !trim.equalsIgnoreCase("null")) vehicle.setSubModel(trim.trim().toUpperCase());
                        if (!engine.isEmpty() && !engine.equalsIgnoreCase("null")) vehicle.setEngineSize(engine.trim() + "L");
                        if (!drive.isEmpty() && !drive.equalsIgnoreCase("null")) vehicle.setDrivetrain(drive.trim().toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("NHTSA Network Parser Timeout fallback log: " + e.getMessage());
            vehicle.setYear("2020");
            vehicle.setMake("UNKNOWN");
            vehicle.setModel("CHASSIS UNMAPPED");
        }
    }

    // --- CUSTOMER ACTIONS SECTION (SaaS TENANT FIREWALL SECURED) ---
    @PostMapping("/customers/register")
    public String registerCustomer(@ModelAttribute("newCustomer") @Valid Customer customer, BindingResult result, RedirectAttributes ra) {
        // 🔒 Extract the active request thread's tenant workspace ID automatically
        String activeTenant = TenantContext.getCurrentTenant();
        if (activeTenant == null) {
            activeTenant = "xyz-auto"; // Safe fallback developer tracker token
        }

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.newCustomer", result);
            ra.addFlashAttribute("newCustomer", customer);
            return "redirect:/?tab=customers&error=true";
        }

        // 👑 THE SaaS FIREWALL STAMPS:
        // Force-bind the record row strictly to the logged-in user's shop context parameters
        customer.setTenantId(activeTenant);
        customer.setCustomerNumber("CUST-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        customerRepository.save(customer);
        return "redirect:/?tab=customers";
    }

    // =========================================================
    // 👥 UPDATE CLIENT INDEX PROFILE LEDGER RECORD (SaaS SECURED)
    // =========================================================
    @PostMapping("/customers/update/{id}")
    public String updateCustomer(@PathVariable("id") Long id,
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("lastName") String lastName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("email") String email,
                                 @RequestParam("street") String street,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("zip") String zip,
                                 @RequestParam(value = "taxExempt", required = false, defaultValue = "false") boolean taxExempt) {

        // 🔒 Extract the active request thread's tenant workspace ID automatically
        String activeTenant = TenantContext.getCurrentTenant();
        if (activeTenant == null) {
            activeTenant = "xyz-auto";
        }

        // 👑 ROW-LEVEL SECURITY CHECK:
        // Swapped out findById to ensure updates only execute if the row belongs to this active tenant
        customerRepository.findByIdAndTenantId(id, activeTenant).ifPresent(customer -> {
            if (firstName != null && !firstName.trim().isEmpty()) customer.setFirstName(firstName.trim());
            customer.setLastName(lastName != null ? lastName.trim() : "");
            if (phone != null && !phone.trim().isEmpty()) customer.setPhone(phone.trim());
            customer.setEmail(email != null ? email.trim() : "");
            customer.setStreet(street != null ? street.trim() : "");
            customer.setCity(city != null ? city.trim() : "");
            customer.setState(state != null ? state.trim() : "");
            customer.setZip(zip != null ? zip.trim() : "");

            // Persist the live commercial fleet toggle selection securely [Shopmonkey auto repair app]
            customer.setTaxExempt(taxExempt);

            customerRepository.save(customer);
        });

        return "redirect:/?tab=customers";
    }

    // --- VEHICLE ACTIONS SECTION ---
    @PostMapping("/vehicles/register")
    public String registerCustomerVehicle(@ModelAttribute("newVehicle") Vehicle vehicle, @RequestParam("customerId") Long customerId) {
        customerRepository.findById(customerId).ifPresent(customer -> {
            vehicle.setCustomer(customer);

            String rawVin = vehicle.getVin() != null ? vehicle.getVin().trim().toUpperCase() : "";
            vehicle.setVin(rawVin);

            // ⚡ EXECUTE LIVE NHTSA API DECODER
            decodeVinLive(vehicle, rawVin);

            vehicleRepository.save(vehicle);
        });
        return "redirect:/?tab=vehicles";
    }

    @PostMapping("/vehicles/update/{id}")
    public String updateVehicleProfileInline(@PathVariable("id") Long id,
                                             @RequestParam("year") String year, // <-- 🔑 Fixed to String
                                             @RequestParam("make") String make,
                                             @RequestParam("model") String model,
                                             @RequestParam("subModel") String subModel,
                                             @RequestParam("engineSize") String engineSize,
                                             @RequestParam("drivetrain") String drivetrain,
                                             @RequestParam("licensePlate") String licensePlate,
                                             @RequestParam("vin") String vin) {
        vehicleRepository.findById(id).ifPresent(vehicle -> {
            vehicle.setYear(year != null ? year.trim() : ""); // <-- 🔑 Clean String Assignment
            vehicle.setMake(make != null ? make.trim().toUpperCase() : "");
            vehicle.setModel(model != null ? model.trim().toUpperCase() : "");
            vehicle.setSubModel(subModel != null ? subModel.trim().toUpperCase() : "");
            vehicle.setEngineSize(engineSize != null ? engineSize.trim() : "");
            vehicle.setDrivetrain(drivetrain != null ? drivetrain.trim().toUpperCase() : "");
            vehicle.setLicensePlate(licensePlate != null ? licensePlate.trim().toUpperCase() : "");
            vehicle.setVin(vin != null ? vin.trim().toUpperCase() : "");
            vehicleRepository.save(vehicle);
        });
        return "redirect:/?tab=vehicles";
    }

    @PostMapping("/vehicles/update-odometer")
    public String processOdometerLogs(@RequestParam("workOrderId") Long workOrderId,
                                      @RequestParam(value = "odometerIn", required = false) Integer odoIn,
                                      @RequestParam(value = "odometerOut", required = false) Integer odoOut) {
        workOrderRepository.findById(workOrderId).ifPresent(order -> {
            if (odoIn != null) order.setOdometerIn(odoIn);
            if (odoOut != null) order.setOdometerOut(odoOut);
            workOrderRepository.save(order);
        });
        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    @GetMapping("/orders/toggle-view")
    public String toggleOrdersViewMode(@RequestParam("showAll") boolean showAll, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("showAllOrders", showAll);
        return "redirect:/?tab=orders";
    }

    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatusMatrix(@PathVariable("id") Long id,
                                          @RequestParam("status") WorkOrderStatus status,
                                          @RequestParam("paymentStatus") PaymentStatus paymentStatus,
                                          @RequestParam("amountPaid") BigDecimal amountPaid) {
        workOrderRepository.findById(id).ifPresent(order -> {
            order.setStatus(status);
            order.setPaymentStatus(paymentStatus);
            order.setAmountPaid(amountPaid);
            if (status == WorkOrderStatus.ARCHIVED || status == WorkOrderStatus.COMPLETED) {
                order.setDateClosed(LocalDateTime.now());
            }
            workOrderRepository.save(order);
        });
        return "redirect:/?tab=orders&focusedWorkOrderId=" + id;
    }

    // =========================================================
    // ➕ AUTOMATED WORK ORDER GENERATION FROM LEDGER TAB
    // =========================================================
    @PostMapping("/work-orders/create")
    public String createNewWorkOrderFromTab(@RequestParam("vehicleId") Long vehicleId,
                                            RedirectAttributes redirectAttributes) {
        Vehicle vehicleProfile = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle ID mapping link: " + vehicleId));

        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setVehicle(vehicleProfile);
        newWorkOrder.setStatus(WorkOrderStatus.OPENED);
        newWorkOrder.setPaymentStatus(PaymentStatus.UNPAID);
        newWorkOrder.setAmountPaid(BigDecimal.ZERO);

        String generatedInvoiceTrackingToken = "WO-" + (System.currentTimeMillis() % 100000);
        newWorkOrder.setInvoiceNumber(generatedInvoiceTrackingToken);

        workOrderRepository.save(newWorkOrder);

        redirectAttributes.addFlashAttribute("successMessage", "New Work Order successfully generated on the floor!");
        return "redirect:/?tab=orders";
    }

    // =========================================================
    // 👁️ INVOICE FOCUS LEDGER SELECTOR VIEWPORT (404 FIX)
    // =========================================================
    @GetMapping("/work-orders/focus/{id}")
    public String focusSpecificWorkOrderSheetFromLedger(@PathVariable("id") Long id) {
        return "redirect:/?tab=orders&focusedWorkOrderId=" + id;
    }

    // =========================================================
    // 📦 REVISED DIRECT-PROCUREMENT INTAKE ENGINE
    // =========================================================a
    @PostMapping("/work-orders/add-item")
    public String addDirectPurchaseLineItemToWorkOrder(
            @RequestParam("workOrderId") Long workOrderId,
            @RequestParam("itemType") String itemType,
            @RequestParam("description") String description,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("retailPrice") BigDecimal retailPrice,
            @RequestParam(value = "partNumber", required = false) String partNumber,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "wholesaleCost", required = false) BigDecimal wholesaleCost,
            RedirectAttributes redirectAttributes) {

        WorkOrder workOrderRecord = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid work order reference ID: " + workOrderId));

        // 🔒 SAFETY LOCK ENGINE CORE BLOCK: Prevents database pollution on finalized tickets
        if (workOrderRecord.getStatus() != WorkOrderStatus.OPENED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Security Alert: This invoice record is locked out and cannot accept line changes!");
            return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
        }

        LineItem newLineItem = new LineItem();
        newLineItem.setWorkOrder(workOrderRecord);
        newLineItem.setDescription(description.trim());
        newLineItem.setQuantity(quantity);
        newLineItem.setRetailPrice(retailPrice);

        // 🔍 Map incoming section flows type-safely to database constraints
        if ("LABOR".equalsIgnoreCase(itemType)) {
            newLineItem.setItemType(LineItemType.LABOR);
            newLineItem.setPartNumber(null);
            newLineItem.setVendor(null);
            newLineItem.setWholesaleCost(BigDecimal.ZERO);
        } else if ("PART".equalsIgnoreCase(itemType)) {
            newLineItem.setItemType(LineItemType.PART);
            newLineItem.setPartNumber(partNumber != null ? partNumber.trim().toUpperCase() : "N/A");
            newLineItem.setVendor(vendor != null ? vendor.trim().toUpperCase() : "STOCK");
            newLineItem.setWholesaleCost(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        } else if ("MISC".equalsIgnoreCase(itemType)) {
            // Miscellaneous packages behave like a part with no trackable SKU overhead
            newLineItem.setItemType(LineItemType.PART);
            newLineItem.setPartNumber("MISC-MAT");
            newLineItem.setVendor(vendor != null && !vendor.trim().isEmpty() ? vendor.trim().toUpperCase() : "SHOP");
            newLineItem.setWholesaleCost(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        }

        // Inside your addDirectPurchaseLineItemToWorkOrder method...
        BigDecimal finalizedRetailPrice = retailPrice;

        if (LineItemType.PART.name().equals(itemType)) {
            newLineItem.setPartNumber(partNumber);
            newLineItem.setVendor(vendor);
            BigDecimal cleanCost = wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO;
            newLineItem.setWholesaleCost(cleanCost);

            // 👑 AUTO-MATRIX ACTIVATION CHECK:
            if (finalizedRetailPrice == null || finalizedRetailPrice.compareTo(BigDecimal.ZERO) == 0) {
                finalizedRetailPrice = WorkOrder.calculateMatrixRetailPrice(cleanCost);
            }
        } else {
            newLineItem.setPartNumber(null);
            newLineItem.setVendor(null);
            newLineItem.setWholesaleCost(BigDecimal.ZERO);
        }

        newLineItem.setRetailPrice(finalizedRetailPrice);

        lineItemRepository.save(newLineItem);

        redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to invoice worksheet ledger!");
        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    // 🚚 CORRECTED DYNAMIC SHOP INSTANCE VARIABLES
    // Pulls directly from your exact property keys in application.properties
    @Value("${shop.config.live.name:XYZ Auto Services LLC}")
    private String shopName;

    @Value("${shop.config.live.address:27 Hicks Av, Annapolis, MD 21401}")
    private String shopAddress;

    @Value("${shop.config.live.phone:(410) 555-0199}")
    private String shopPhone;

    // =========================================================
    // 🖨️ CUSTOM INVOICE PDF EXPORT LAYOUT ROUTER
    // =========================================================
    @GetMapping("/orders/{id}/print")
    public String renderPrintableInvoiceSheet(@PathVariable("id") Long id, Model model) {

        System.out.println("⚡ [DEBUG] INVOICE PRINT ENDPOINT ACTIVATED! Target Work Order ID: " + id);

        java.util.Optional<WorkOrder> orderOpt = workOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return "redirect:/?tab=orders";
        }

        WorkOrder order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("vehicle", order.getVehicle());
        model.addAttribute("customer", order.getVehicle().getCustomer());
        model.addAttribute("lineItems", order.getLineItems());

        model.addAttribute("partsSubtotal", order.getPartsSubtotal());
        model.addAttribute("laborSubtotal", order.getLaborSubtotal());
        model.addAttribute("grandTotal", order.getTotalDue());
        model.addAttribute("amountPaid", order.getAmountPaid());
        model.addAttribute("balanceDue", order.getRemainingBalance());

        // 👑 PASS YOUR EXACT ENVIRONMENT METRICS TO THE VIEW LAYER:
        model.addAttribute("shopName", shopName);
        model.addAttribute("shopAddress", shopAddress);
        model.addAttribute("shopPhone", shopPhone);

        return "print-invoice";
    }

}