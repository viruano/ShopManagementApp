package com.autorepair.shop;

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

    // --- MASTER CORE ROUTER: MANAGES WORKSPACE STATE ENGINE ---
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

        // Retain the active browser panel selection tab state across redirects
        model.addAttribute("activeTab", tab);
        model.addAttribute("editCustomerId", editCustomerId);
        model.addAttribute("editVehicleId", editVehicleId);
        model.addAttribute("editItemId", editItemId);

        // Populate base form bindings for modal/panel intakes
        if (!model.containsAttribute("newCustomer")) model.addAttribute("newCustomer", new Customer());
        if (!model.containsAttribute("newVehicle")) model.addAttribute("newVehicle", new Vehicle());
        if (!model.containsAttribute("newLineItem")) model.addAttribute("newLineItem", new LineItem());

        // Always provide global dropdown dependencies
        List<Customer> allCustomers = customerRepository.findAll();
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        model.addAttribute("customers", allCustomers);
        model.addAttribute("vehicles", allVehicles);

        // 👥 PANEL 1 LAYER: CUSTOMERS MANAGEMENT CHECKSETS
        if ("customers".equals(tab)) {
            List<Customer> filteredCustomers = (search != null && !search.trim().isEmpty()) ?
                    customerRepository.findByCustomerNumberContainingIgnoreCaseOrPhoneContainingOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            search.trim(), search.trim(), search.trim(), search.trim()) : allCustomers;
            model.addAttribute("customers", filteredCustomers);
            model.addAttribute("currentSearch", search);
        }

        // 🚗 PANEL 2 LAYER: VEHICLES FLEET CHECKSETS
        if ("vehicles".equals(tab)) {
            List<Vehicle> filteredVehicles = (vehicleSearch != null && !vehicleSearch.trim().isEmpty()) ?
                    vehicleRepository.findByLicensePlateContainingIgnoreCaseOrVinContainingIgnoreCase(
                            vehicleSearch.trim(), vehicleSearch.trim()) : allVehicles;
            model.addAttribute("vehicles", filteredVehicles);
            model.addAttribute("currentVehicleSearch", vehicleSearch);
        }

        // 📋 PANEL 3 LAYER: INVOICING WORK ORDERS ARCHITECTURE
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

    // 🏎️ FIXED LIVE EXTERNAL DATA ENGINE USING STRING BUFFER PARSING
    private void decodeVinLive(Vehicle vehicle, String vin) {
        if (vin == null || vin.trim().length() < 10) return;

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            // String apiUrl = "https://dot.gov" + vin.trim() + "?format=json";
            String apiUrl = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/"+ vin.trim()  + "?format=json";

            // ⚡ STEP 1: FETCH AS RAW STRING TO BYPASS CONVERTER TRAPS
            String jsonRaw = restTemplate.getForObject(apiUrl, String.class);

            if (jsonRaw != null && !jsonRaw.trim().isEmpty()) {
                // ⚡ STEP 2: USE OBJECTMAPPER TO PARSE THE TREE SAFE AND DIRECT
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonRaw);

                if (root != null && root.has("Results")) {
                    com.fasterxml.jackson.databind.JsonNode results = root.get("Results");

                    // The decodevinvalues endpoint returns an array where results are inside the first index element [0]
                    if (results.isArray() && results.size() > 0) {
                        com.fasterxml.jackson.databind.JsonNode vehicleData = results.get(0);

                        // Extract flat fields directly from the first element node array
                        String year = vehicleData.path("ModelYear").asText("");
                        String make = vehicleData.path("Make").asText("");
                        String model = vehicleData.path("Model").asText("");
                        String trim = vehicleData.path("Trim").asText("");
                        String engine = vehicleData.path("DisplacementL").asText("");
                        String drive = vehicleData.path("DriveType").asText("");

                        // Map properties cleanly to your entity fields if values exist
                        if (!year.isEmpty() && !year.equalsIgnoreCase("null")) vehicle.setYear(year.trim());
                        if (!make.isEmpty() && !make.equalsIgnoreCase("null")) vehicle.setMake(make.trim());
                        if (!model.isEmpty() && !model.equalsIgnoreCase("null")) vehicle.setModel(model.trim());
                        if (!trim.isEmpty() && !trim.equalsIgnoreCase("null")) vehicle.setSubModel(trim.trim());
                        if (!engine.isEmpty() && !engine.equalsIgnoreCase("null")) vehicle.setEngineSize(engine.trim() + "L");
                        if (!drive.isEmpty() && !drive.equalsIgnoreCase("null")) vehicle.setDrivetrain(drive.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("NHTSA Network Parser Timeout: " + e.getMessage());
            // Safe resilient database fallbacks if internet line drop or server downtime occurs
            vehicle.setYear("2020");
            vehicle.setMake("Unknown");
            vehicle.setModel("Chassis Unmapped");
        }
    }

    // --- CUSTOMER ACTIONS SECTION ---
    @PostMapping("/customers/register")
    public String registerCustomer(@ModelAttribute("newCustomer") @Valid Customer customer, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.newCustomer", result);
            ra.addFlashAttribute("newCustomer", customer);
            return "redirect:/?tab=customers&error=true";
        }
        customer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        customerRepository.save(customer);
        return "redirect:/?tab=customers";
    }

    @PostMapping("/customers/update/{id}")
    public String updateCustomer(@PathVariable("id") Long id,
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("lastName") String lastName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("email") String email,
                                 @RequestParam("street") String street,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("zip") String zip) {
        customerRepository.findById(id).ifPresent(customer -> {
            if (firstName != null && !firstName.trim().isEmpty()) customer.setFirstName(firstName.trim());
            customer.setLastName(lastName != null ? lastName.trim() : "");
            if (phone != null && !phone.trim().isEmpty()) customer.setPhone(phone.trim());
            customer.setEmail(email != null ? email.trim() : "");
            customer.setStreet(street != null ? street.trim() : "");
            customer.setCity(city != null ? city.trim() : "");
            customer.setState(state != null ? state.trim() : "");
            customer.setZip(zip != null ? zip.trim() : "");
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
                                             @RequestParam("year") String year,
                                             @RequestParam("make") String make,
                                             @RequestParam("model") String model,
                                             @RequestParam("subModel") String subModel,
                                             @RequestParam("engineSize") String engineSize,
                                             @RequestParam("drivetrain") String drivetrain,
                                             @RequestParam("licensePlate") String licensePlate,
                                             @RequestParam("vin") String vin) {
        vehicleRepository.findById(id).ifPresent(vehicle -> {
            vehicle.setYear(year != null ? year.trim() : "");
            vehicle.setMake(make != null ? make.trim() : "");
            vehicle.setModel(model != null ? model.trim() : "");
            vehicle.setSubModel(subModel != null ? subModel.trim() : "");
            vehicle.setEngineSize(engineSize != null ? engineSize.trim() : "");
            vehicle.setDrivetrain(drivetrain != null ? drivetrain.trim() : "");
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

    // --- WORK ORDER ACTIONS SECTION ---
    @PostMapping("/orders/create")
    public String createWorkOrderTicket(@RequestParam("vehicleId") Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle != null) {
            WorkOrder order = new WorkOrder();
            long nextSequence = workOrderRepository.count() + 1001;
            order.setInvoiceNumber("WO-" + nextSequence);
            order.setVehicle(vehicle);
            order.setStatus(WorkOrderStatus.OPENED);
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOdometerIn(vehicle.getOdometerIn() != null ? vehicle.getOdometerIn() : 0);
            order.setOdometerOut(vehicle.getOdometerOut() != null ? vehicle.getOdometerOut() : 0);
            workOrderRepository.save(order);
        }
        return "redirect:/?tab=orders";
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

    // --- LINE ITEMS INVOICING ACTIONS ---
    @PostMapping("/line-items/add")
    public String addLineItemToWorkOrder(@ModelAttribute("newLineItem") LineItem item, @RequestParam("workOrderId") Long workOrderId) {
        workOrderRepository.findById(workOrderId).ifPresent(order -> {
            item.setWorkOrder(order);
            if (item.getCostPrice() == null) item.setCostPrice(BigDecimal.ZERO);
            lineItemRepository.save(item);
        });
        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    @PostMapping("/line-items/update/{id}")
    public String processLineItemUpdateInline(@PathVariable("id") Long id,
                                              @RequestParam("description") String desc,
                                              @RequestParam("quantity") Double qty,
                                              @RequestParam("retailPrice") BigDecimal retail,
                                              @RequestParam("workOrderId") Long workOrderId) {
        lineItemRepository.findById(id).ifPresent(item -> {
            item.setDescription(desc);
            item.setQuantity(qty);
            item.setRetailPrice(retail);
            lineItemRepository.save(item);
        });
        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    @GetMapping("/line-items/delete/{id}")
    public String removeLineItemFromWorksheet(@PathVariable("id") Long id, @RequestParam("workOrderId") Long workOrderId) {
        lineItemRepository.deleteById(id);
        return "redirect:/?tab=orders&focusedWorkOrderId=" + workOrderId;
    }
}
