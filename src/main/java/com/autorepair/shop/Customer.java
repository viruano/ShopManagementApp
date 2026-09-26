package com.autorepair.shop;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;
    private String email;

    @Column(name = "customer_number", unique = true)
    private String customerNumber;

    private String street;
    private String city;
    private String state;
    private String zip;

    // Default Constructor (Required by JPA)
    public Customer() {}

    // Convenience Constructor
    public Customer(String firstName, String lastName, String phone, String email,
                    String customerNumber, String street, String city, String state, String zip) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.customerNumber = customerNumber;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    // ==========================================
    // Updated Getters and Setters
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + firstName + " " + lastName + '\'' +
                ", customerNumber='" + customerNumber + '\'' +
                '}';
    }
}