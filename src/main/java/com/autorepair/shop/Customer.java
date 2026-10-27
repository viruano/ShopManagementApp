package com.autorepair.shop;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_number", unique = true, nullable = false, length = 50)
    private String customerNumber;

    @NotBlank(message = "First name is strictly required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is strictly required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Phone number is strictly required")
    @Size(min = 7, max = 20, message = "Please enter a valid telephone number layout")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Email(message = "Please enter a valid email address layout (e.g. name@domain.com)")
    @Size(max = 100, message = "Email address cannot exceed 100 characters")
    @Column(name = "email", nullable = true, length = 100)
    private String email;

    // 🟢 ADDED: Unified, safe B2B optional property architecture field
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    @Column(name = "company_name", nullable = true, length = 150)
    private String companyName;

    @Size(max = 150, message = "Street path layout cannot exceed 150 characters")
    @Column(name = "street", nullable = true, length = 150)
    private String street;

    @Size(max = 100, message = "City name cannot exceed 100 characters")
    @Column(name = "city", nullable = true, length = 100)
    private String city;

    @Size(max = 50, message = "State identity code cannot exceed 50 characters")
    @Column(name = "state", nullable = true, length = 50)
    private String state;

    @Size(max = 12, message = "Postal ZIP routing code cannot exceed 12 characters")
    @Column(name = "zip", nullable = true, length = 12)
    private String zip;

    @Column(name = "tenant_id", nullable = true, length = 50)
    private String tenantId;

    @Column(name = "tax_exempt", nullable = true)
    private Boolean taxExempt;

    // =========================================================
    // ⚙️ SYSTEM CONSTRUCTORS (LIFECYCLE SEEDERS)
    // =========================================================
    public Customer() {}

    // =========================================================
    // 📑 ACCESSIBILITY GETTER / SETTER API METHODS
    // =========================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // 🟢 ADDED: Company Name getter and setter
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName != null ? companyName.trim() : null; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public String getTenantId() {
        if (this.tenantId == null || this.tenantId.trim().isEmpty()) {
            return "xyz-auto";
        }
        return this.tenantId;
    }
    public void setTenantId(String tenantId) { this.tenantId = tenantId != null ? tenantId.trim() : null; }

    public boolean isTaxExempt() { return this.taxExempt != null && this.taxExempt; }
    public boolean getTaxExempt() { return isTaxExempt(); }
    public void setTaxExempt(boolean taxExempt) { this.taxExempt = taxExempt; }
}
