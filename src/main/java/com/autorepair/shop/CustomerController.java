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

    // =========================================================
    // 🗄️ AUTOMATED DEPENDENCY INJECTION PORT DECK
    // =========================================================
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    private VinDecoderService vinDecoderService;

    // =========================================================
    // 🌐 SaaS MULTI-DOMAIN ARCHITECTURE TRAFFIC ROUTER
    // =========================================================
    @GetMapping("/portfolio")
    public String renderConsultingPortfolioDeck(Model model) {
        System.out.println("☁️ [TRAFFIC ENGINE] Serving Orion Data Architecture corporate portfolio layout...");

        // Formally passes your active tenant profile keys down to seal template requirements
        model.addAttribute("activeTenant", "orion-core");
        return "portfolio"; // Compiles and renders portfolio.html seamlessly!
    }


    // =========================================================
    // 👑 THE MASTER MULTI-TENANT CONTEXT INTERCEPTOR
    // =========================================================
    // Spring Boot executes this method BEFORE every route to fill your
    // @ModelAttribute("activeTenant") argument with the real logged-in shop!
    @ModelAttribute("activeTenant")
    public String populateRequestActiveTenantContext() {
        // 1. Sync directly with Spring Security to see who is physically logged in
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        String tenant = "xyz-auto"; // Ultimate secure production shop fallback core default

        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            String username = auth.getName().toLowerCase();

            // 🔒 AUTOMATED ACCOUNT WORKSPACE MATRICES MATCHING:
            if (username.contains("demo") || username.contains("sandbox") || username.contains("main-st")) {
                tenant = "main-st-demo";
            } else {
                tenant = "xyz-auto";
            }
        }

        // 2. Force-reconcile the thread context so your repository queries lock down safely
        TenantContext.setCurrentTenant(tenant);

        System.out.println("🔒 [SaaS CONTEXT SECURITY] Resolved Active Workspace Token: " + tenant);
        return tenant;
    }

    // =========================================================
    // 🌐 SYSTEM FRONT DOOR GATEKEEPER
    // =========================================================
    @GetMapping("/")
    public String crossDomainTrafficInterceptor() {
        // Forces all naked root web traffic straight over to your portfolio layout
        return "redirect:/portfolio";
    }

    // =========================================================
    // 📊 THE INTERNAL WORKSPACE CONTROL CONSOLE
    // =========================================================
    @GetMapping("/dashboard") // 👑 PATH CHANGED HERE TO PREVENT OVERWRITES!
    public String viewDashboard(
            @RequestParam(value = "tab", required = false, defaultValue = "customers") String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "vehicleSearch", required = false) String vehicleSearch,
            @RequestParam(value = "focusedWorkOrderId", required = false) Long focusedWorkOrderId,
            @RequestParam(value = "editItemId", required = false) Long editItemId,
            @RequestParam(value = "editCustomerId", required = false) Long editCustomerId,
            @RequestParam(value = "editVehicleId", required = false) Long editVehicleId,
            @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
            Model model) {

        // Retain the active browser panel selection tab state across redirects
        model.addAttribute("activeTab", tab);
        model.addAttribute("editCustomerId", editCustomerId);
        model.addAttribute("editVehicleId", editVehicleId);
        model.addAttribute("editItemId", editItemId);

        // Populate base form bindings for modal/panel intakes
        if (!model.containsAttribute("newCustomer")) model.addAttribute("newCustomer", new Customer());
        if (!model.containsAttribute("newVehicle")) model.addAttribute("newVehicle", new Vehicle());
        if (!model.containsAttribute("newLineItem")) model.addAttribute("newLineItem", new LineItem());

        // 🔒 TENANT-ISOLATED CUSTOMER & VEHICLE DATA ENGINE
        List<Customer> tenantCustomers = customerRepository.findByTenantId(activeTenant);
        model.addAttribute("customers", tenantCustomers);

        List<Vehicle> tenantVehicles = vehicleRepository.findByTenantId(activeTenant);
        model.addAttribute("vehicles", tenantVehicles);

        // 👥 PANEL 1 LAYER: CUSTOMERS MANAGEMENT CHECKSETS (TENANT SECURED)
        if ("customers".equals(tab)) {
            List<Customer> filteredCustomers = (search != null && !search.trim().isEmpty()) ?
                    customerRepository.searchTenantCustomers(search.trim(), activeTenant) : tenantCustomers;
            model.addAttribute("customers", filteredCustomers);
            model.addAttribute("currentSearch", search);
        }

        // 🚗 PANEL 2 LAYER: VEHICLES FLEET CHECKSETS (TENANT SECURED)
        if ("vehicles".equals(tab)) {
            List<Vehicle> filteredVehicles;
            if (vehicleSearch != null && !vehicleSearch.trim().isEmpty()) {
                filteredVehicles = vehicleRepository.searchTenantVehicles(vehicleSearch.trim(), activeTenant);
            } else {
                filteredVehicles = tenantVehicles;
            }
            model.addAttribute("vehicles", filteredVehicles);
            model.addAttribute("currentVehicleSearch", vehicleSearch);
        }

        // 📋 PANEL 3 LAYER: INVOICING WORK ORDERS ARCHITECTURE (TENANT ISOLATED)
        List<WorkOrder> visibleOrders;
        boolean showAll = model.containsAttribute("showAllOrders") && (boolean) model.getAttribute("showAllOrders");
        model.addAttribute("showAllOrders", showAll);

        if ("orders".equals(tab) && search != null && !search.trim().isEmpty()) {
            visibleOrders = workOrderRepository.searchTenantOrders(search.trim(), activeTenant);
            model.addAttribute("currentSearch", search);
        } else if (showAll) {
            visibleOrders = workOrderRepository.findByTenantId(activeTenant);
        } else {
            visibleOrders = workOrderRepository.findByStatusAndTenantId(WorkOrderStatus.OPENED, activeTenant);
        }
        model.addAttribute("workOrders", visibleOrders);

        // Isolate totals and line rows if a specific invoice is selected
        if (focusedWorkOrderId != null) {
            // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
            // Swapped out findById to ensure an explicit multi-tenant firewall cross-check
            workOrderRepository.findByIdAndTenantId(focusedWorkOrderId, activeTenant).ifPresent(order -> {
                model.addAttribute("focusedWorkOrder", order);
                model.addAttribute("focusedWorkOrderId", focusedWorkOrderId);
                model.addAttribute("lineItems", order.getLineItems());

                // Aggregate financial totals live for this specific ticket scope [Shopmonkey auto repair app]
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

        // =========================================================
        // 📊 SaaS FINANCIAL ACCUMULATOR ENGINE MATRICES
        // =========================================================
        // Automatically tallies financial totals strictly for the current shop session context
        BigDecimal liveTenantRevenue = visibleOrders.stream()
                .map(WorkOrder::getTotalDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal liveTenantPaymentsCollected = visibleOrders.stream()
                .map(WorkOrder::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal liveTenantReceivables = visibleOrders.stream()
                .map(WorkOrder::getRemainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bind the precise aggregated variables straight to your template tokens
        model.addAttribute("liveTenantRevenue", liveTenantRevenue);
        model.addAttribute("liveTenantPaymentsCollected", liveTenantPaymentsCollected);
        model.addAttribute("liveTenantReceivables", liveTenantReceivables);

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

                        String yearStr = vehicleData.path("ModelYear").asText("");
                        String make = vehicleData.path("Make").asText("");
                        String model = vehicleData.path("Model").asText("");
                        String trim = vehicleData.path("Trim").asText("");
                        String engine = vehicleData.path("DisplacementL").asText("");
                        String drive = vehicleData.path("DriveType").asText("");

                        // 👑 THE CRITICAL INT UNBOXING FIX:
                        // Converts the text string year token cleanly to a primitive int for your entity model
                        if (!yearStr.isEmpty() && !yearStr.equalsIgnoreCase("null")) {
                            try {
                                vehicle.setYear(Integer.parseInt(yearStr.trim()));
                            } catch (NumberFormatException nfe) {
                                vehicle.setYear(2020); // Defensive baseline default fallback
                            }
                        }

                        if (!make.isEmpty() && !make.equalsIgnoreCase("null"))
                            vehicle.setMake(make.trim().toUpperCase());
                        if (!model.isEmpty() && !model.equalsIgnoreCase("null"))
                            vehicle.setModel(model.trim().toUpperCase());
                        if (!trim.isEmpty() && !trim.equalsIgnoreCase("null"))
                            vehicle.setTrim(trim.trim().toUpperCase());
                        if (!engine.isEmpty() && !engine.equalsIgnoreCase("null"))
                            vehicle.setEngineSize(engine.trim() + "L");
                        if (!drive.isEmpty() && !drive.equalsIgnoreCase("null"))
                            vehicle.setDrivetrain(drive.trim().toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("NHTSA Network Parser Timeout fallback log: " + e.getMessage());
            vehicle.setYear(2020); // 👑 Aligned to int literal type matching requirements
            vehicle.setMake("UNKNOWN");
            vehicle.setModel("CHASSIS UNMAPPED");
        }
    }
    // --- CUSTOMER ACTIONS SECTION (SaaS TENANT FIREWALL SECURED) ---
    @PostMapping("/customers/register")
    public String registerCustomer(@ModelAttribute("newCustomer") @Valid Customer customer,
                                   @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                   BindingResult result,
                                   RedirectAttributes ra) {
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
                                 @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("lastName") String lastName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("email") String email,
                                 @RequestParam("street") String street,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("zip") String zip,
                                 @RequestParam(value = "taxExempt", required = false, defaultValue = "false") boolean taxExempt) {

        // 👑 ROW-LEVEL SECURITY CHECK:
        // Now compiles perfectly and restricts updates strictly to the active shop session
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

    // =========================================================
    // 🚗 VEHICLE ACTIONS SECTION (SaaS FIREWALL + BALANCED VIN DECODER)
    // =========================================================
    @PostMapping("/vehicles/register")
    public String registerVehicle(@ModelAttribute("newVehicle") @jakarta.validation.Valid Vehicle vehicle,
                                  BindingResult result,
                                  @RequestParam(value = "customerId", required = false) Long customerId,
                                  @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                  RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.newVehicle", result);
            ra.addFlashAttribute("newVehicle", vehicle);
            return "redirect:/?tab=vehicles&error=true";
        }

        // 👑 1. EXECUTE INLINE DECODER HYDRATION
        // Safely updates the year, make, and model variables directly on the existing object mesh
        if (vehicle.getVin() != null && !vehicle.getVin().trim().isEmpty()) {
            vinDecoderService.hydrateVehicleSpecs(vehicle);
        }

        // 👑 2. SECURE THE RELATIONAL ACCOUNT OWNER GRAPH (FIXES UNASSIGNED NULLS)
        // Explicitly binds the vehicle to the managed Customer row to force-write the customer_id column
        if (customerId != null) {
            customerRepository.findByIdAndTenantId(customerId, activeTenant).ifPresent(vehicle::setCustomer);
        }

        // 👑 3. APPLY THE SaaS MULTI-TENANT ROW PROTECTION LOCK
        // Compiles perfectly using the injected global parameter!
        vehicle.setTenantId(activeTenant);

        // Clean up text filters before saving to your PostgreSQL data tables
        if (vehicle.getMake() != null) vehicle.setMake(vehicle.getMake().trim().toUpperCase());
        if (vehicle.getModel() != null) vehicle.setModel(vehicle.getModel().trim().toUpperCase());
        if (vehicle.getVin() != null) vehicle.setVin(vehicle.getVin().trim().toUpperCase());
        if (vehicle.getLicensePlate() != null) vehicle.setLicensePlate(vehicle.getLicensePlate().trim().toUpperCase());

        vehicleRepository.save(vehicle);
        return "redirect:/?tab=vehicles";
    }

    // =========================================================
    // 🚗 UPDATE VEHICLE SERVICE FILE LEDGER (SaaS FIREWALL SECURED)
    // =========================================================
    @PostMapping("/vehicles/update/{id}")
    public String updateVehicle(@PathVariable("id") Long id,
                                @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                @RequestParam(value = "year", required = false) Integer year,
                                @RequestParam(value = "make", required = false) String make,
                                @RequestParam(value = "model", required = false) String model,
                                @RequestParam(value = "vin", required = false) String vin,
                                @RequestParam(value = "licensePlate", required = false) String licensePlate,
                                @RequestParam(value = "trim", required = false) String trim,
                                @RequestParam(value = "engineSize", required = false) String engineSize,
                                @RequestParam(value = "drivetrain", required = false) String drivetrain) {

        // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
        // Now compiles perfectly and secures the vehicle file updates strictly within tenant rows
        vehicleRepository.findByIdAndTenantId(id, activeTenant).ifPresent(vehicle -> {
            if (year != null && year.intValue() > 0) {
                vehicle.setYear(year.intValue());
            }
            if (make != null && !make.trim().isEmpty()) vehicle.setMake(make.trim().toUpperCase());
            if (model != null && !model.trim().isEmpty()) vehicle.setModel(model.trim().toUpperCase());
            if (vin != null && !vin.trim().isEmpty()) vehicle.setVin(vin.trim().toUpperCase());
            if (licensePlate != null && !licensePlate.trim().isEmpty()) {
                vehicle.setLicensePlate(licensePlate.trim().toUpperCase());
            }

            // Handle optional specifications variables cleanly
            vehicle.setTrim(trim != null ? trim.trim().toUpperCase() : null);
            vehicle.setEngineSize(engineSize != null ? engineSize.trim().toUpperCase() : null);
            vehicle.setDrivetrain(drivetrain != null ? drivetrain.trim().toUpperCase() : null);

            vehicleRepository.save(vehicle);
        });

        return "redirect:/?tab=vehicles";
    }

    // =========================================================
    // 🚗 PROCESS VEHICLE ODOMETER METRIC UPDATES (SaaS SECURED)
    // =========================================================
    @PostMapping("/vehicles/update-odometer")
    public String processOdometerLogs(@RequestParam("workOrderId") Long workOrderId,
                                      @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                      @RequestParam(value = "odometerIn", required = false) Integer odoIn,
                                      @RequestParam(value = "odometerOut", required = false) Integer odoOut) {

        // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
        // Confines odometer write-actions strictly to rows belonging to the active workspace thread
        workOrderRepository.findByIdAndTenantId(workOrderId, activeTenant).ifPresent(order -> {
            if (odoIn != null) order.setOdometerIn(odoIn);
            if (odoOut != null) order.setOdometerOut(odoOut);
            workOrderRepository.save(order);
            System.out.println("✅ [SaaS RECONCILE] Odometer metrics updated securely under tenant context.");
        });

        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    @GetMapping("/orders/toggle-view")
    public String toggleOrdersViewMode(@RequestParam("showAll") boolean showAll, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("showAllOrders", showAll);
        return "redirect:/?tab=orders";
    }

    // =========================================================
    // 📋 WORK ORDER INTAKE INTERFACE: SaaS Tenant Safe Binding
    // =========================================================
    @PostMapping("/orders/create")
    public String createWorkOrder(@ModelAttribute("newWorkOrder") WorkOrder workOrder,
                                  @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                  @RequestParam("vehicleId") Long vehicleId) {

        // 👑 2. APPLY THE CRITICAL SaaS SECURED FIREWALL STAMP:
        // Compiles perfectly using the injected global parameter!
        workOrder.setTenantId(activeTenant);

        // 🚗 3. Stitch your existing vehicle relationship mapping securely
        if (vehicleId != null) {
            vehicleRepository.findByIdAndTenantId(vehicleId, activeTenant).ifPresent(workOrder::setVehicle);
        }

        // Generate professional incremental invoice sequence markers if empty
        if (workOrder.getInvoiceNumber() == null || workOrder.getInvoiceNumber().trim().isEmpty()) {
            workOrder.setInvoiceNumber("WO-" + (System.currentTimeMillis() % 100000));
        }

        workOrderRepository.save(workOrder);
        return "redirect:/?tab=orders";
    }

    // =========================================================
    // 📋 UPDATE REPAIR SHEET METRICS MATRIX (SaaS SECURED)
    // =========================================================
    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatusMatrix(@PathVariable("id") Long id,
                                          @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                          @RequestParam("status") WorkOrderStatus status,
                                          @RequestParam("paymentStatus") PaymentStatus paymentStatus,
                                          @RequestParam("amountPaid") BigDecimal amountPaid) {

        // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
        // Now compiles perfectly using the injected global parameter!
        workOrderRepository.findByIdAndTenantId(id, activeTenant).ifPresent(order -> {
            order.setStatus(status);
            order.setPaymentStatus(paymentStatus);
            order.setAmountPaid(amountPaid != null ? amountPaid : java.math.BigDecimal.ZERO);

            if (status == WorkOrderStatus.ARCHIVED || status == WorkOrderStatus.COMPLETED) {
                order.setDateClosed(java.time.LocalDateTime.now());
            }

            workOrderRepository.save(order);
            System.out.println("✅ [SaaS SECURITY] Work Order status updated securely under Tenant: " + activeTenant);
        });

        return "redirect:/?tab=orders&focusedWorkOrderId=" + id;
    }

    // =========================================================
    // ➕ AUTOMATED WORK ORDER GENERATION FROM LEDGER TAB
    // =========================================================
    @PostMapping("/work-orders/create")
    public String createNewWorkOrderFromTab(@RequestParam("vehicleId") Long vehicleId,
                                            @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
                                            RedirectAttributes redirectAttributes) {

        // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
        // Restricts vehicle lookups strictly to the active workspace session context
        Vehicle vehicleProfile = vehicleRepository.findByIdAndTenantId(vehicleId, activeTenant)
                .orElseThrow(() -> new IllegalArgumentException("Security Exception: Invalid vehicle workspace mapping context: " + vehicleId));

        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setVehicle(vehicleProfile);
        newWorkOrder.setStatus(WorkOrderStatus.OPENED);
        newWorkOrder.setPaymentStatus(PaymentStatus.UNPAID);
        newWorkOrder.setAmountPaid(BigDecimal.ZERO);

        // 👑 THE SaaS FIREWALL STAMP:
        // Binds the newly created invoice row directly to the logged-in user's shop tenant
        newWorkOrder.setTenantId(activeTenant);

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
    // 📦 REVISED DIRECT-PROCUREMENT INTAKE ENGINE (SaaS SECURED)
    // =========================================================
    @PostMapping("/work-orders/add-item")
    public String addDirectPurchaseLineItemToWorkOrder(
            @RequestParam("workOrderId") Long workOrderId,
            @ModelAttribute("activeTenant") String activeTenant, // 👑 INJECTED AUTOMATICALLY VIA GLOBAL INTERCEPTOR!
            @RequestParam("itemType") String itemType,
            @RequestParam("description") String description,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("retailPrice") BigDecimal retailPrice,
            @RequestParam(value = "partNumber", required = false) String partNumber,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "wholesaleCost", required = false) BigDecimal wholesaleCost,
            RedirectAttributes redirectAttributes) {

        // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
        // Swapped out findById to ensure parts can only be added to invoices belonging strictly to this active shop
        WorkOrder workOrderRecord = workOrderRepository.findByIdAndTenantId(workOrderId, activeTenant)
                .orElseThrow(() -> new IllegalArgumentException("Security Exception: Invalid work order workspace reference mapping context: " + workOrderId));

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
            newLineItem.setItemType(LineItemType.PART);
            newLineItem.setPartNumber("MISC-MAT");
            newLineItem.setVendor(vendor != null && !vendor.trim().isEmpty() ? vendor.trim().toUpperCase() : "SHOP");
            newLineItem.setWholesaleCost(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        }

        BigDecimal finalizedRetailPrice = retailPrice;

        if (LineItemType.PART.name().equalsIgnoreCase(itemType) || "MISC".equalsIgnoreCase(itemType)) {
            BigDecimal cleanCost = wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO;

            // 👑 AUTO-MATRIX ACTIVATION CHECK:
            // Applies your automated sliding matrix if the advisor leaves the retail price blank [Shopmonkey auto repair app]
            if (finalizedRetailPrice == null || finalizedRetailPrice.compareTo(BigDecimal.ZERO) == 0) {
                finalizedRetailPrice = WorkOrder.calculateMatrixRetailPrice(cleanCost);
            }
        }

        newLineItem.setRetailPrice(finalizedRetailPrice);
        lineItemRepository.save(newLineItem);

        redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to invoice worksheet ledger!");
        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    // =========================================================
    // 🖨️ DYNAMIC SaaS PRINT RECEIPT ROUTE CONTROLLER (SECURED)
    // =========================================================
    @GetMapping("/orders/{id}/print")
    public String renderPrintableInvoiceSheet(@PathVariable("id") Long id,
                                              @ModelAttribute("activeTenant") String activeTenant, // 👑 Injected via global interceptor
                                              Model model) {

        System.out.println("⚡ [PRINTER ENGINE] Initializing invoice generation loop for ticket ID: " + id);

        // 👑 ROW-LEVEL SECURITY INTERCEPTOR:
        // Confines printable receipt scanning strictly to the active workspace
        java.util.Optional<WorkOrder> orderOpt = workOrderRepository.findByIdAndTenantId(id, activeTenant);

        if (orderOpt.isEmpty()) {
            System.err.println("❌ Printer Security Error: Work Order ID " + id + " does not belong to tenant: " + activeTenant);
            return "redirect:/?tab=orders";
        }

        WorkOrder order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("vehicle", order.getVehicle());
        model.addAttribute("customer", order.getVehicle().getCustomer());
        model.addAttribute("lineItems", order.getLineItems());

        // Aggregate financial totals live for this specific ticket view window scope [Shopmonkey auto repair app]
        model.addAttribute("partsSubtotal", order.getPartsSubtotal());
        model.addAttribute("laborSubtotal", order.getLaborSubtotal());
        model.addAttribute("grandTotal", order.getTotalDue());
        model.addAttribute("amountPaid", order.getAmountPaid());
        model.addAttribute("balanceDue", order.getRemainingBalance());

        // =========================================================
        // 👑 DYNAMIC SaaS DATABASE WORKSPACE BRANDING INJECTOR
        // =========================================================
        // Pulls invoice shop branding data straight from the database dynamically
        Tenant tenantProfile = tenantRepository.findById(activeTenant)
                .orElse(new Tenant(activeTenant, "XYZ Auto Services LLC", "27 Hicks Av, Annapolis, MD 21401", "(443) 325-6663", false));

        model.addAttribute("shopName", tenantProfile.getShopName());
        model.addAttribute("shopAddress", tenantProfile.getAddress());
        model.addAttribute("shopPhone", tenantProfile.getPhone());
        model.addAttribute("isDemoMode", tenantProfile.isDemoMode());

        return "print-invoice";
    } // 👑 CORRECTED: Formally closes the public String method block cleanly!

}
