package com.autorepair.shop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final VinDecoderService vinDecoderService;

    // Spring Boot automatically injects our fresh lookup service right here
    public CustomerController(CustomerRepository customerRepository,
                              VehicleRepository vehicleRepository,
                              VinDecoderService vinDecoderService) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.vinDecoderService = vinDecoderService;
    }

    @GetMapping("/")
    public String viewDashboard(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("vehicles", vehicleRepository.findAll());
        model.addAttribute("newCustomer", new Customer());
        model.addAttribute("newVehicle", new Vehicle());
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
        // If a 17-character VIN is passed, enrich our model attributes automatically via government databases
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
}
