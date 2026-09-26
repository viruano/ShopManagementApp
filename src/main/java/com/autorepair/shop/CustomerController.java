package com.autorepair.shop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;

    // Dependency Injection: Spring automatically feeds your repository into this controller
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // 1. Display Dashboard view with a list of all active customers
    @GetMapping("/")
    public String viewDashboard(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("newCustomer", new Customer()); // Preps a blank object for the form
        return "index";
    }

    // 2. Handle the Customer Registration Form Submission
    @PostMapping("/customers/register")
    public String registerCustomer(@ModelAttribute("newCustomer") Customer customer) {
        // Automatically generate a clean, unique account tracking number for the business
        // Example output: CUST-7B93
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        customer.setCustomerNumber("CUST-" + uniqueSuffix);

        // Save the customer record cleanly to your database (H2 locally or PostgreSQL in Railway)
        customerRepository.save(customer);

        // Redirect back to the dashboard homepage to refresh and show the updated list immediately
        return "redirect:/";
    }
}