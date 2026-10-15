package com.autorepair.shop;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VinDecoderService {

    private final RestTemplate restTemplate;

    public VinDecoderService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 👑 INLINE ENTIY HYDRATOR
     * Accepts the form-bound vehicle instance and populates its specs directly
     * without breaking existing database relationship graphs or dropping customer IDs.
     */
    public void hydrateVehicleSpecs(Vehicle vehicle) {
        if (vehicle == null || vehicle.getVin() == null || vehicle.getVin().trim().isEmpty()) {
            return;
        }

        try {
            String url = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + vehicle.getVin().trim() + "?format=json";
            VinResponse response = restTemplate.getForObject(url, VinResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                VinResponse.VehicleData data = response.getResults().get(0);

                // 👑 THE CRITICAL DATA-TYPE CONVERSION FIX:
                // Safely checks if the string year exists, parses it to an int, and protects against conversion loops
                if (data.getModelYear() != null && !data.getModelYear().trim().isEmpty()) {
                    try {
                        vehicle.setYear(Integer.parseInt(data.getModelYear().trim()));
                    } catch (NumberFormatException e) {
                        vehicle.setYear(2020); // Safe baseline fallback if the API returns an invalid character format
                    }
                }

                vehicle.setMake(data.getMake());
                vehicle.setModel(data.getModel());

                if (data.getTrim() != null) {
                    vehicle.setTrim(data.getTrim().trim().toUpperCase());
                } else {
                    vehicle.setTrim(null);
                }

                vehicle.setDrivetrain(data.getDriveType());
                vehicle.setEngineSize(data.getDisplacementL() != null ? data.getDisplacementL() + "L" : null);
                vehicle.setEngineCode(data.getEngineModel());
            }
        } catch (Exception e) {
            System.err.println("❌ NHTSA Flat API Error: " + e.getMessage());
        }
    }
}
