package com.autorepair.shop;

import jakarta.persistence.*;
import java.lang.String;

@Entity
@Table(name = "labor_guide_catalog")
public class LaborGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // e.g., "Brakes", "Engine Electrical", "Suspension"

    @Column(name = "job_action", nullable = false)
    private String jobAction; // e.g., "Replace Alternator", "Front Brake Pads - R&R"

    @Column(name = "standard_hours", nullable = false)
    private Double standardHours; // Standard industry "Book Time" (e.g., 1.5)

    public LaborGuide() {}

    public LaborGuide(String category, String jobAction, Double standardHours) {
        this.category = category;
        this.jobAction = jobAction;
        this.standardHours = standardHours;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getJobAction() { return jobAction; }
    public void setJobAction(String jobAction) { this.jobAction = jobAction; }
    public Double getStandardHours() { return standardHours; }
    public void setStandardHours(Double standardHours) { this.standardHours = standardHours; }
}
