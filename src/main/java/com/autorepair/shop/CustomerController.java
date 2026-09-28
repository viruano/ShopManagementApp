package com.autorepair.shop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

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
    public String viewDashboard(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Customer> customers;

        // INTERCEPT ROUTE: If an active search term is typed, query our new matching repository rules
        if (search != null && !search.trim().isEmpty()) {
            customers = customerRepository.findByCustomerNumberContainingIgnoreCaseOrPhoneContaining(search.trim(), search.trim());
            model.addAttribute("currentSearch", search.trim()); // Sends back the string to keep it inside the text box layout
        } else {
            // Default Fallback: If no search string is present, pull up the whole client index list normally
            customers = customerRepository.findAll();
        }

        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<LineItem> lineItems = lineItemRepository.findAll();

        // Financial Calculation Logic Summary Loop
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
    public String registerCustomer(@ModelAttribute("newCustomer") Customer customer) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
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
