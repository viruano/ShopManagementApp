package com.autorepair.shop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final LineItemRepository lineItemRepository;
    private final VinDecoderService vinDecoderService;

    // Injecting the new LineItemRepository right into our core business controller router
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
    public String viewDashboard(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("vehicles", vehicleRepository.findAll());
        model.addAttribute("lineItems", lineItemRepository.findAll()); // Track all active workshop charges
        model.addAttribute("newCustomer", new Customer());
        model.addAttribute("newVehicle", new Vehicle());
        model.addAttribute("newLineItem", new LineItem()); // Preps a blank billing row for the front-end view
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

    // --- NEW ENDPOINT: Processes incoming Parts and Labor Billing Actions ---
    @PostMapping("/line-items/add")
    public String addLineItem(@ModelAttribute("newLineItem") LineItem lineItem, @RequestParam("vehicleId") Long vehicleId) {
        // Fetch the corresponding vehicle profile and bind the line item relationally
        vehicleRepository.findById(vehicleId).ifPresent(lineItem::setVehicle);

        // Save the part line item or labor hour charge row into your database storage
        lineItemRepository.save(lineItem);

        // Redirect right back to our new Work Orders tab panel screen state
        return "redirect:/?tab=orders";
    }
}
