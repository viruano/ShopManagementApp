package com.autorepair.shop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;


@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final LineItemRepository lineItemRepository;
    private final VinDecoderService vinDecoderService;

    public CustomerController(CustomerRepository customerRepository,
                              VehicleRepository vehicleRepository,
                              LineItemRepository lineItemRepository,
                              VinDecoderService vinDecoderService) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.lineItemRepository = lineItemRepository;
        this.vinDecoderService = vinDecoderService;
    }

    @GetMapping("/")
    public String viewDashboard(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "vehicleSearch", required = false) String vehicleSearch,
            Model model) {

        // --- 1. Handle Customer Index Search Filters ---
        List<Customer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = customerRepository.findByCustomerNumberContainingIgnoreCaseOrPhoneContaining(search.trim(), search.trim());
            model.addAttribute("currentSearch", search.trim());
        } else {
            customers = customerRepository.findAll();
        }

        // --- 2. NEW: Handle Vehicle Index Fleet Search Filters ---
        List<Vehicle> vehicles;
        if (vehicleSearch != null && !vehicleSearch.trim().isEmpty()) {
            vehicles = vehicleRepository.findByLicensePlateContainingIgnoreCaseOrVinContainingIgnoreCase(vehicleSearch.trim(), vehicleSearch.trim());
            model.addAttribute("currentVehicleSearch", vehicleSearch.trim()); // Holds the text value inside the input box layout
        } else {
            vehicles = vehicleRepository.findAll();
        }

        List<LineItem> lineItems = lineItemRepository.findAll();

        // --- 3. Financial Calculation Performance Loop ---
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalCost = java.math.BigDecimal.ZERO;
        for (LineItem item : lineItems) {
            java.math.BigDecimal qty = java.math.BigDecimal.valueOf(item.getQuantity());
            totalRevenue = totalRevenue.add(item.getRetailPrice().multiply(qty));
            if (item.getCostPrice() != null) {
                totalCost = totalCost.add(item.getCostPrice().multiply(qty));
            }
        }

        model.addAttribute("customers", customers);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("lineItems", lineItems);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("totalProfit", totalRevenue.subtract(totalCost));

        model.addAttribute("newCustomer", new Customer());
        model.addAttribute("newVehicle", new Vehicle());
        model.addAttribute("newLineItem", new LineItem());

        return "index";
    }

    @PostMapping("/customers/register")
    public String registerCustomer(@Valid @ModelAttribute("newCustomer") Customer customer, BindingResult result, Model model) {
        // Intercept validation failures (like missing names or malformed emails)
        if (result.hasErrors()) {
            // Re-populate dashboard tracking lists so the page renders normally
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("vehicles", vehicleRepository.findAll());
            model.addAttribute("lineItems", lineItemRepository.findAll());

            // Re-populate math calculators
            java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalCost = java.math.BigDecimal.ZERO;
            for (LineItem item : lineItemRepository.findAll()) {
                java.math.BigDecimal qty = java.math.BigDecimal.valueOf(item.getQuantity());
                totalRevenue = totalRevenue.add(item.getRetailPrice().multiply(qty));
                if (item.getCostPrice() != null) totalCost = totalCost.add(item.getCostPrice().multiply(qty));
            }
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalCost", totalCost);
            model.addAttribute("totalProfit", totalRevenue.subtract(totalCost));

            model.addAttribute("newVehicle", new Vehicle());
            model.addAttribute("newLineItem", new LineItem());

            // Return back to the console without saving, bringing the validation errors along
            return "index";
        }

        // Standard save pipeline paths execute only if constraints pass perfectly
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        customer.setCustomerNumber("CUST-" + uniqueSuffix);
        customerRepository.save(customer);
        return "redirect:/?tab=customers";
    }

    @PostMapping("/vehicles/register")
    public String registerVehicle(@ModelAttribute("newVehicle") Vehicle vehicle, @RequestParam("customerId") Long customerId) {
        if (vehicle.getVin() != null && vehicle.getVin().trim().length() == 17) {
            Vehicle apiData = vinDecoderService.decodeVin(vehicle.getVin().trim());
            if (apiData.getMake() != null) {
                vehicle.setYear(apiData.getYear());
                vehicle.setMake(apiData.getMake());
                vehicle.setModel(apiData.getModel());
                vehicle.setSubModel(apiData.getSubModel());
                vehicle.setDrivetrain(apiData.getDrivetrain());
                vehicle.setEngineSize(apiData.getEngineSize());
                vehicle.setEngineCode(apiData.getEngineCode());
            }
        }
        customerRepository.findById(customerId).ifPresent(vehicle::setCustomer);
        vehicleRepository.save(vehicle);
        return "redirect:/?tab=vehicles";
    }

    @PostMapping("/line-items/add")
    public String addLineItem(@ModelAttribute("newLineItem") LineItem lineItem, @RequestParam("vehicleId") Long vehicleId) {
        vehicleRepository.findById(vehicleId).ifPresent(lineItem::setVehicle);
        lineItemRepository.save(lineItem);
        return "redirect:/?tab=orders";
    }
}
