package com.autorepair.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VinResponse {

    @JsonProperty("Results")
    private List<VehicleData> results;

    public List<VehicleData> getResults() { return results; }
    public void setResults(List<VehicleData> results) { this.results = results; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VehicleData {
        @JsonProperty("ModelYear") private String modelYear;
        @JsonProperty("Make") private String make;
        @JsonProperty("Model") private String model;
        @JsonProperty("Series") private String series;
        @JsonProperty("DriveType") private String driveType;
        @JsonProperty("DisplacementL") private String displacementL;
        @JsonProperty("EngineModel") private String engineModel;

        // Getters and Setters
        public String getModelYear() { return modelYear; }
        public void setModelYear(String modelYear) { this.modelYear = modelYear; }
        public String getMake() { return make; }
        public void setMake(String make) { this.make = make; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getSeries() { return series; }
        public void setSeries(String series) { this.series = series; }
        public String getDriveType() { return driveType; }
        public void setDriveType(String driveType) { this.driveType = driveType; }
        public String getDisplacementL() { return displacementL; }
        public void setDisplacementL(String displacementL) { this.displacementL = displacementL; }
        public String getEngineModel() { return engineModel; }
        public void setEngineModel(String engineModel) { this.engineModel = engineModel; }
    }
}
