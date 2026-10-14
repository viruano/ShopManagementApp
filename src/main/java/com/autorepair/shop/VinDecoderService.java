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

                // Inject the decoded specs directly onto the un-broken form reference
                vehicle.setYear(data.getModelYear());
                vehicle.setMake(data.getMake());
                vehicle.setModel(data.getModel());
                vehicle.setTrim(data.getTrim());
                vehicle.setDrivetrain(data.getDriveType());
                vehicle.setEngineSize(data.getDisplacementL() != null ? data.getDisplacementL() + "L" : null);
                vehicle.setEngineCode(data.getEngineModel());
            }
        } catch (Exception e) {
            System.err.println("❌ NHTSA Flat API Error: " + e.getMessage());
        }
    }
}
