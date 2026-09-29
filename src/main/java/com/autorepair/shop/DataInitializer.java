package com.autorepair.shop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LaborGuideRepository laborGuideRepository;

    public DataInitializer(LaborGuideRepository laborGuideRepository) {
        this.laborGuideRepository = laborGuideRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed standard auto mechanic book time metrics if the catalog is empty
        if (laborGuideRepository.count() == 0) {
            laborGuideRepository.save(new LaborGuide("Brakes", "Front Brake Pads - Remove & Replace", 1.5));
            laborGuideRepository.save(new LaborGuide("Brakes", "Brake Rotor - R&R (Per Side)", 1.0));
            laborGuideRepository.save(new LaborGuide("Engine Electrical", "Replace Alternator", 2.2));
            laborGuideRepository.save(new LaborGuide("Engine Electrical", "Replace Starter Motor", 1.8));
            laborGuideRepository.save(new LaborGuide("Suspension", "Front Wheel Alignment - Standard", 1.2));
            laborGuideRepository.save(new LaborGuide("Engine Mechanical", "Water Pump Replacement", 3.5));
            System.out.println("⚙️ Real-Time Labor Guide Catalog successfully seeded with industry standard book times.");
        }
    }
}
