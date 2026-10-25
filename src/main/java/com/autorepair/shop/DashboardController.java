package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private WorkOrderRepository workOrderRepository;

    // 🛡️ DYNAMIC PROFILE INJECTION TOGGLE: Reads directly from application.properties
    @Value("${shop.config.multi-tenant:true}")
    private boolean isMultiTenantEnabled;

    // =========================================================
    // 👑 THE PRO-GRADE AUTOMATED CONTEXT INTERCEPTOR
    // =========================================================
    @ModelAttribute("activeTenant")
    public String populateRequestActiveTenantContext() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        String tenant = "xyz-auto"; // Absolute secure system baseline fallback default

        // 🧠 ENVIRONMENT PROTECTION FIREWALL:
        // If running locally (multi-tenant=false), it completely skips lookups and locks the session context!
        if (isMultiTenantEnabled && auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            String username = auth.getName().toLowerCase();

            // Reconcile user tokens to isolated cloud workspace shards dynamically
            if (username.contains("demo") || username.contains("sandbox") || username.contains("main-st")) {
                tenant = "main-st-demo";
            }
        }

        TenantContext.setCurrentTenant(tenant);
        System.out.println("🔒 [SaaS CONTEXT SECURITY] Resolved Active Workspace Token: " + tenant + " (Multi-Tenant Mode: " + isMultiTenantEnabled + ")");
        return tenant;
    }

    // =========================================================
    // 🌐 SaaS MULTI-DOMAIN ARCHITECTURE TRAFFIC ROUTERS
    // =========================================================
    @GetMapping("/portfolio")
    public String renderConsultingPortfolioDeck(Model model) {
        System.out.println("☁️ [TRAFFIC ENGINE] Serving Orion Data Architecture corporate portfolio layout...");
        model.addAttribute("activeTenant", "orion-core");
        return "portfolio";
    }

    @GetMapping("/")
    public String crossDomainTrafficInterceptor(@RequestHeader("Host") String host) {
        System.out.println("📡 [TRAFFIC ENGINE] Incoming request host: " + host);
        if (host.toLowerCase().startsWith("shopapp.") || host.contains("shopmanagementapp-production")) {
            System.out.println("🚗 Serving the Automotive App Custom Login Template...");
            return "login";
        }
        return "redirect:/portfolio";
    }

    @GetMapping("/login")
    public String showCustomLoginGateView() {
        System.out.println("🔒 [SECURITY ENGINE] Compiling branded B2B evaluation login canvas...");
        return "login";
    }

    // =========================================================
    // 📊 THE INTERNAL WORKSPACE CONTROL CONSOLE
    // =========================================================
    @GetMapping("/dashboard")
    public String viewDashboard(
            @RequestParam(value = "tab", required = false, defaultValue = "customers") String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "vehicleSearch", required = false) String vehicleSearch,
            @RequestParam(value = "focusedWorkOrderId", required = false) Long focusedWorkOrderId,
            @RequestParam(value = "editItemId", required = false) Long editItemId,
            @RequestParam(value = "editCustomerId", required = false) Long editCustomerId,
            @RequestParam(value = "editVehicleId", required = false) Long editVehicleId,
            @ModelAttribute("activeTenant") String activeTenant,
            Model model) {

        // 🟢 FORCED NAVIGATION GUARD: If focusing a specific repair ticket scope, explicitly force the view state to the orders tab console drawer!
        if (focusedWorkOrderId != null) {
            tab = "orders";
        }

        String safeTab = tab.trim().toLowerCase();
        model.addAttribute("activeTab", safeTab);
        model.addAttribute("currentTab", safeTab); // 🟢 SYNC BONUS: Hydrates both parameter conventions to completely eliminate HTML variable mismatch bugs!
        model.addAttribute("editCustomerId", editCustomerId);
        model.addAttribute("editVehicleId", editVehicleId);
        model.addAttribute("editItemId", editItemId);

        if (!model.containsAttribute("newCustomer")) model.addAttribute("newCustomer", new Customer());
        if (!model.containsAttribute("newVehicle")) model.addAttribute("newVehicle", new Vehicle());
        if (!model.containsAttribute("newLineItem")) model.addAttribute("newLineItem", new LineItem());

        List<Customer> tenantCustomers = customerRepository.findByTenantId(activeTenant);
        model.addAttribute("customers", tenantCustomers);

        List<Vehicle> tenantVehicles = vehicleRepository.findByTenantId(activeTenant);
        model.addAttribute("vehicles", tenantVehicles);

        if ("customers".equals(safeTab)) {
            List<Customer> filteredCustomers = (search != null && !search.trim().isEmpty()) ?
                    customerRepository.searchTenantCustomers(search.trim(), activeTenant) : tenantCustomers;
            model.addAttribute("customers", filteredCustomers);
            model.addAttribute("currentSearch", search);
        }

        if ("vehicles".equals(safeTab)) {
            List<Vehicle> filteredVehicles = (vehicleSearch != null && !vehicleSearch.trim().isEmpty()) ?
                    vehicleRepository.searchTenantVehicles(vehicleSearch.trim(), activeTenant) : tenantVehicles;
            model.addAttribute("vehicles", filteredVehicles);
            model.addAttribute("currentVehicleSearch", vehicleSearch);
        }

        List<WorkOrder> visibleOrders;
        boolean showAll = model.containsAttribute("showAllOrders") && (boolean) model.getAttribute("showAllOrders");
        model.addAttribute("showAllOrders", showAll);

        if ("orders".equals(safeTab) && search != null && !search.trim().isEmpty()) {
            visibleOrders = workOrderRepository.searchTenantOrders(search.trim(), activeTenant);
            model.addAttribute("currentSearch", search);
        } else if (showAll) {
            visibleOrders = workOrderRepository.findByTenantId(activeTenant);
        } else {
            visibleOrders = workOrderRepository.findByStatusAndTenantId(WorkOrderStatus.OPENED, activeTenant);
        }
        model.addAttribute("workOrders", visibleOrders);

        if (focusedWorkOrderId != null) {
            workOrderRepository.findByIdAndTenantId(focusedWorkOrderId, activeTenant).ifPresent(order -> {
                model.addAttribute("focusedWorkOrder", order);
                model.addAttribute("focusedWorkOrderId", focusedWorkOrderId);
                model.addAttribute("lineItems", order.getLineItems());

                model.addAttribute("totalParts", order.getPartsSubtotal() != null ? order.getPartsSubtotal() : BigDecimal.ZERO);
                model.addAttribute("totalLabor", order.getLaborSubtotal() != null ? order.getLaborSubtotal() : BigDecimal.ZERO);
                model.addAttribute("totalRevenue", order.getTotalDue() != null ? order.getTotalDue() : BigDecimal.ZERO);
                model.addAttribute("totalProfit", order.getGrandProfitDollars() != null ? order.getGrandProfitDollars() : BigDecimal.ZERO);
                model.addAttribute("amountPaid", order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO);
                model.addAttribute("remainingBalance", order.getRemainingBalance() != null ? order.getRemainingBalance() : BigDecimal.ZERO);
            });
        } else {
            BigDecimal globalRev = BigDecimal.ZERO;
            if (visibleOrders != null && !visibleOrders.isEmpty()) {
                globalRev = visibleOrders.stream()
                        .map(order -> order.getTotalDue() != null ? order.getTotalDue() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            model.addAttribute("totalParts", BigDecimal.ZERO);
            model.addAttribute("totalLabor", BigDecimal.ZERO);
            model.addAttribute("totalRevenue", globalRev);
            model.addAttribute("amountPaid", BigDecimal.ZERO);
            model.addAttribute("remainingBalance", globalRev);
        }

        BigDecimal liveTenantRevenue = BigDecimal.ZERO;
        BigDecimal liveTenantPaymentsCollected = BigDecimal.ZERO;
        BigDecimal liveTenantReceivables = BigDecimal.ZERO;

        if (visibleOrders != null && !visibleOrders.isEmpty()) {
            liveTenantRevenue = visibleOrders.stream()
                    .map(order -> order.getTotalDue() != null ? order.getTotalDue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            liveTenantPaymentsCollected = visibleOrders.stream()
                    .map(order -> order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            liveTenantReceivables = visibleOrders.stream()
                    .map(order -> order.getRemainingBalance() != null ? order.getRemainingBalance() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        model.addAttribute("liveTenantRevenue", liveTenantRevenue);
        model.addAttribute("liveTenantPaymentsCollected", liveTenantPaymentsCollected);
        model.addAttribute("liveTenantReceivables", liveTenantReceivables);

        model.addAttribute("laborCatalog", List.of(
                new LaborGuide("BRAKES", "Front Brake Pads & Rotors Replacement", 2.0),
                new LaborGuide("ENGINE", "Synthetic Engine Oil & Filter Service", 0.5),
                new LaborGuide("SUSPENSION", "Four-Wheel Alignment Alignment Service", 1.5),
                new LaborGuide("DIAGNOSTIC", "Electrical System Scanning Fault Check", 1.0)
        ));

        return "index"; // Points directly to templates/index.html
    }
}
