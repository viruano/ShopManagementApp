package com.autorepair.shop;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VinDecoderService {

    private final RestTemplate restTemplate;

    public VinDecoderService() {
        this.restTemplate = new RestTemplate();
    }

    public Vehicle decodeVin(String vin) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(vin);

        try {
            // Your exact, optimized flat-format endpoint link!
            String url = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + vin + "?format=json";
            VinResponse response = restTemplate.getForObject(url, VinResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                VinResponse.VehicleData data = response.getResults().get(0);

                // Instantly bind the values without any tedious row parsing loops
                vehicle.setYear(data.getModelYear());
                vehicle.setMake(data.getMake());
                vehicle.setModel(data.getModel());
                vehicle.setSubModel(data.getSeries());
                vehicle.setDrivetrain(data.getDriveType());
                vehicle.setEngineSize(data.getDisplacementL() != null ? data.getDisplacementL() + "L" : null);
                vehicle.setEngineCode(data.getEngineModel());
            }
        } catch (Exception e) {
            System.err.println("NHTSA Flat API Error: " + e.getMessage());
        }
        return vehicle;
    }
}
