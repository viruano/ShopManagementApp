package com.autorepair.shop;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final LaborGuideRepository laborGuideRepository;
    private final InvoicePdfService invoicePdfService; // Injected document engine
    private final VinDecoderService vinDecoderService;

    public CustomerController(CustomerRepository customerRepository,
                              VehicleRepository vehicleRepository,
                              LineItemRepository lineItemRepository,
                              LaborGuideRepository laborGuideRepository,
                              InvoicePdfService invoicePdfService,
                              VinDecoderService vinDecoderService) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.lineItemRepository = lineItemRepository;
        this.laborGuideRepository = laborGuideRepository;
        this.invoicePdfService = invoicePdfService;
        this.vinDecoderService = vinDecoderService;
    }

    @GetMapping("/")
    public String viewDashboard(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "vehicleSearch", required = false) String vehicleSearch,
            Model model) {

        List<Customer> customers = (search != null && !search.trim().isEmpty()) ?
                customerRepository.findByCustomerNumberContainingIgnoreCaseOrPhoneContaining(search.trim(), search.trim()) : customerRepository.findAll();

        List<Vehicle> vehicles = (vehicleSearch != null && !vehicleSearch.trim().isEmpty()) ?
                vehicleRepository.findByLicensePlateContainingIgnoreCaseOrVinContainingIgnoreCase(vehicleSearch.trim(), vehicleSearch.trim()) : vehicleRepository.findAll();

        model.addAttribute("customers", customers);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("lineItems", lineItemRepository.findAll());
        model.addAttribute("laborCatalog", laborGuideRepository.findAll());

        // Margins Tracker Logic
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

        model.addAttribute("newCustomer", new Customer());
        model.addAttribute("newVehicle", new Vehicle());
        model.addAttribute("newLineItem", new LineItem());

        return "index";
    }

    // --- NEW ENDPOINT: Generates and streams down the raw Invoice document file ---
    @GetMapping("/vehicles/{id}/invoice")
    @ResponseBody
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable("id") Long id) {
        // 1. Query the vehicle database record using Data-JPA
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle == null) return ResponseEntity.notFound().build();

        // 2. Fetch only the billing rows specifically linked to this vehicle profile
        List<LineItem> billedItems = lineItemRepository.findByVehicleId(id);

        // 3. Compile the digital document using our OpenPDF layout generator
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(vehicle, billedItems);

        // 4. Stream the binary byte stream directly back to the user browser frame
        String filename = "invoice_" + vehicle.getLicensePlate() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/customers/register")
    public String registerCustomer(@ModelAttribute("newCustomer") Customer customer) {
        customer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        customerRepository.save(customer);
        return "redirect:/?tab=customers";
    }

    @PostMapping("/vehicles/register")
    public String registerVehicle(@ModelAttribute("newVehicle") Vehicle vehicle, @RequestParam("customerId") Long customerId) {
        if (vehicle.getVin() != null && vehicle.getVin().trim().length() == 17) {
            Vehicle apiData = vinDecoderService.decodeVin(vehicle.getVin().trim());
            if (apiData.getMake() != null) {
                vehicle.setYear(apiData.getYear()); vehicle.setMake(apiData.getMake()); vehicle.setModel(apiData.getModel());
                vehicle.setSubModel(apiData.getSubModel()); vehicle.setDrivetrain(apiData.getDrivetrain());
                vehicle.setEngineSize(apiData.getEngineSize()); vehicle.setEngineCode(apiData.getEngineCode());
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
