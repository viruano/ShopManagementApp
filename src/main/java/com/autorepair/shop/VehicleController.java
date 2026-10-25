package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private VinDecoderService vinDecoderService;

    // =========================================================
    // 🚗 VEHICLE ACTIONS SECTION (SaaS FIREWALL + VIN DECODER)
    // =========================================================
    @PostMapping("/register")
    public String registerVehicle(@ModelAttribute("newVehicle") @jakarta.validation.Valid Vehicle vehicle,
                                  BindingResult result,
                                  @RequestParam(value = "customerId", required = false) Long customerId,
                                  @ModelAttribute("activeTenant") String activeTenant,
                                  RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.newVehicle", result);
            ra.addFlashAttribute("newVehicle", vehicle);
            return "redirect:/dashboard?tab=vehicles&error=true";
        }

        if (vehicle.getVin() != null && !vehicle.getVin().trim().isEmpty()) {
            vinDecoderService.hydrateVehicleSpecs(vehicle);
        }

        if (customerId != null) {
            customerRepository.findByIdAndTenantId(customerId, activeTenant).ifPresent(vehicle::setCustomer);
        }

        vehicle.setTenantId(activeTenant);

        if (vehicle.getMake() != null) vehicle.setMake(vehicle.getMake().trim().toUpperCase());
        if (vehicle.getModel() != null) vehicle.setModel(vehicle.getModel().trim().toUpperCase());
        if (vehicle.getVin() != null) vehicle.setVin(vehicle.getVin().trim().toUpperCase());
        if (vehicle.getLicensePlate() != null) vehicle.setLicensePlate(vehicle.getLicensePlate().trim().toUpperCase());

        vehicleRepository.save(vehicle);
        return "redirect:/dashboard?tab=vehicles";
    }

    // =========================================================
    // 🚗 UPDATE VEHICLE SERVICE FILE LEDGER (SaaS FIREWALL SECURED)
    // =========================================================
    @PostMapping("/update/{id}")
    public String updateVehicle(@PathVariable("id") Long id,
                                @ModelAttribute("activeTenant") String activeTenant,
                                @RequestParam(value = "year", required = false) Integer year,
                                @RequestParam(value = "make", required = false) String make,
                                @RequestParam(value = "model", required = false) String model,
                                @RequestParam(value = "vin", required = false) String vin,
                                @RequestParam(value = "licensePlate", required = false) String licensePlate,
                                @RequestParam(value = "trim", required = false) String trim,
                                @RequestParam(value = "engineSize", required = false) String engineSize,
                                @RequestParam(value = "drivetrain", required = false) String drivetrain) {

        vehicleRepository.findByIdAndTenantId(id, activeTenant).ifPresent(vehicle -> {
            if (year != null && year.intValue() > 0) vehicle.setYear(year.intValue());
            if (make != null && !make.trim().isEmpty()) vehicle.setMake(make.trim().toUpperCase());
            if (model != null && !model.trim().isEmpty()) vehicle.setModel(model.trim().toUpperCase());
            if (vin != null && !vin.trim().isEmpty()) vehicle.setVin(vin.trim().toUpperCase());
            if (licensePlate != null && !licensePlate.trim().isEmpty()) vehicle.setLicensePlate(licensePlate.trim().toUpperCase());

            vehicle.setTrim(trim != null ? trim.trim().toUpperCase() : null);
            vehicle.setEngineSize(engineSize != null ? engineSize.trim().toUpperCase() : null);
            vehicle.setDrivetrain(drivetrain != null ? drivetrain.trim().toUpperCase() : null);

            vehicleRepository.save(vehicle);
        });

        return "redirect:/dashboard?tab=vehicles";
    }

    // =========================================================
    // 🏎️ EXTRACTED INTERNAL LIVE DATA STREAM HELPER METHOD
    // =========================================================
    private void decodeVinLive(Vehicle vehicle, String vin) {
        if (vin == null || vin.trim().length() < 10) return;

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String apiUrl = "https://dot.gov" + vin.trim() + "?format=json";
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

                        if (!yearStr.isEmpty() && !yearStr.equalsIgnoreCase("null")) {
                            try {
                                vehicle.setYear(Integer.parseInt(yearStr.trim()));
                            } catch (NumberFormatException nfe) {
                                vehicle.setYear(2020);
                            }
                        }

                        if (!make.isEmpty() && !make.equalsIgnoreCase("null")) vehicle.setMake(make.trim().toUpperCase());
                        if (!model.isEmpty() && !model.equalsIgnoreCase("null")) vehicle.setModel(model.trim().toUpperCase());
                        if (!trim.isEmpty() && !trim.equalsIgnoreCase("null")) vehicle.setTrim(trim.trim().toUpperCase());
                        if (!engine.isEmpty() && !engine.equalsIgnoreCase("null")) vehicle.setEngineSize(engine.trim() + "L");
                        if (!drive.isEmpty() && !drive.equalsIgnoreCase("null")) vehicle.setDrivetrain(drive.trim().toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("NHTSA Network Parser Timeout fallback log: " + e.getMessage());
            vehicle.setYear(2020);
            vehicle.setMake("UNKNOWN");
            vehicle.setModel("CHASSIS UNMAPPED");
        }
    }
}
