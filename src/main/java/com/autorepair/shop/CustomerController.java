package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // =========================================================
    // 👥 REGISTER NEW CLIENT ENDPOINT (SaaS TENANT SECURED)
    // =========================================================
    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("newCustomer") @Valid Customer customer,
                                   @ModelAttribute("activeTenant") String activeTenant,
                                   BindingResult result,
                                   RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.newCustomer", result);
            ra.addFlashAttribute("newCustomer", customer);
            return "redirect:/dashboard?tab=customers&error=true";
        }

        customer.setTenantId(activeTenant);
        customer.setCustomerNumber("CUST-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        customerRepository.save(customer);
        return "redirect:/dashboard?tab=customers";
    }

    // =========================================================
    // 👥 UPDATE CLIENT PROFILE LEDGER RECORD (SaaS SECURED)
    // =========================================================
    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable("id") Long id,
                                 @ModelAttribute("activeTenant") String activeTenant,
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("lastName") String lastName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("email") String email,
                                 @RequestParam("street") String street,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("zip") String zip,
                                 @RequestParam(value = "taxExempt", required = false, defaultValue = "false") boolean taxExempt) {

        customerRepository.findByIdAndTenantId(id, activeTenant).ifPresent(customer -> {
            if (firstName != null && !firstName.trim().isEmpty()) customer.setFirstName(firstName.trim());
            customer.setLastName(lastName != null ? lastName.trim() : "");
            if (phone != null && !phone.trim().isEmpty()) customer.setPhone(phone.trim());
            customer.setEmail(email != null ? email.trim() : "");
            customer.setStreet(street != null ? street.trim() : "");
            customer.setCity(city != null ? city.trim() : "");
            customer.setState(state != null ? state.trim() : "");
            customer.setZip(zip != null ? zip.trim() : "");
            customer.setTaxExempt(taxExempt);

            customerRepository.save(customer);
        });

        return "redirect:/dashboard?tab=customers";
    }
}
