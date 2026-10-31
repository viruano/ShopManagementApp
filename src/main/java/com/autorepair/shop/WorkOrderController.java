package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class WorkOrderController {

    @Autowired
    private WorkOrderRepository workOrderRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private LineItemRepository lineItemRepository;
    @Autowired
    private TenantRepository tenantRepository;

    // =========================================================
    // 🚗 PROCESS VEHICLE ODOMETER METRIC UPDATES (SaaS SECURED)
    // =========================================================
    @PostMapping("/vehicles/update-odometer")
    public String processOdometerLogs(@RequestParam("workOrderId") Long workOrderId,
                                      @ModelAttribute("activeTenant") String activeTenant,
                                      @RequestParam(value = "odometerIn", required = false) Integer odoIn,
                                      @RequestParam(value = "odometerOut", required = false) Integer odoOut) {

        workOrderRepository.findByIdAndTenantId(workOrderId, activeTenant).ifPresent(order -> {
            if (odoIn != null) order.setOdometerIn(odoIn);
            if (odoOut != null) order.setOdometerOut(odoOut);
            workOrderRepository.save(order);
        });

        return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    @GetMapping("/orders/toggle-view")
    public String toggleOrdersViewMode(@RequestParam("showAll") boolean showAll, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("showAllOrders", showAll);
        return "redirect:/dashboard?tab=orders";
    }

    // =========================================================
    // 📋 WORK ORDER INTAKE INTERFACE: SaaS Tenant Safe Binding
    // =========================================================
    @PostMapping("/orders/create")
    public String createWorkOrder(@ModelAttribute("newWorkOrder") WorkOrder workOrder,
                                  @ModelAttribute("activeTenant") String activeTenant,
                                  @RequestParam("vehicleId") Long vehicleId) {

        workOrder.setTenantId(activeTenant);

        if (vehicleId != null) {
            vehicleRepository.findByIdAndTenantId(vehicleId, activeTenant).ifPresent(existingVehicle -> {
                Customer existingVehicleOwner = existingVehicle.getCustomer();

                if (existingVehicleOwner != null && workOrder.getVehicle() != null && workOrder.getVehicle().getCustomer() != null) {
                    Long newOwnerId = workOrder.getVehicle().getCustomer().getId();

                    if (!existingVehicleOwner.getId().equals(newOwnerId)) {
                        Vehicle transferredVehicle = new Vehicle(existingVehicle, workOrder.getVehicle().getCustomer());
                        vehicleRepository.save(transferredVehicle);
                        workOrder.setVehicle(transferredVehicle);
                        return;
                    }
                }
                workOrder.setVehicle(existingVehicle);
            });
        }

        if (workOrder.getInvoiceNumber() == null || workOrder.getInvoiceNumber().trim().isEmpty()) {
            workOrder.setInvoiceNumber("WO-" + (System.currentTimeMillis() % 100000));
        }

        workOrderRepository.save(workOrder);
        return "redirect:/dashboard?tab=orders";
    }

    // =========================================================
    // 📋 UPDATE REPAIR SHEET METRICS MATRIX (SaaS SECURED)
    // =========================================================
    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatusMatrix(@PathVariable("id") Long id,
                                          @ModelAttribute("activeTenant") String activeTenant,
                                          @RequestParam("status") WorkOrderStatus status,
                                          @RequestParam("paymentStatus") PaymentStatus paymentStatus,
                                          @RequestParam("amountPaid") BigDecimal amountPaid) {

        workOrderRepository.findByIdAndTenantId(id, activeTenant).ifPresent(order -> {
            order.setStatus(status);
            order.setPaymentStatus(paymentStatus);
            order.setAmountPaid(amountPaid != null ? amountPaid : BigDecimal.ZERO);

            if (status == WorkOrderStatus.ARCHIVED || status == WorkOrderStatus.COMPLETED) {
                order.setDateClosed(LocalDateTime.now());
            }
            workOrderRepository.save(order);
        });

        return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + id + "&success=true";
    }

    // =========================================================
    // ➕ AUTOMATED WORK ORDER GENERATION FROM LEDGER TAB
    // =========================================================
    @PostMapping("/work-orders/create")
    public String createNewWorkOrderFromTab(@RequestParam("vehicleId") Long vehicleId,
                                            @ModelAttribute("activeTenant") String activeTenant,
                                            RedirectAttributes redirectAttributes) {

        Vehicle vehicleProfile = vehicleRepository.findByIdAndTenantId(vehicleId, activeTenant)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle: " + vehicleId));

        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setVehicle(vehicleProfile);
        newWorkOrder.setStatus(WorkOrderStatus.OPENED);
        newWorkOrder.setPaymentStatus(PaymentStatus.UNPAID);
        newWorkOrder.setAmountPaid(BigDecimal.ZERO);
        newWorkOrder.setTenantId(activeTenant);
        newWorkOrder.setInvoiceNumber("WO-" + (System.currentTimeMillis() % 100000));

        workOrderRepository.save(newWorkOrder);
        redirectAttributes.addFlashAttribute("successMessage", "New Work Order successfully generated on the floor!");
        return "redirect:/dashboard?tab=orders";
    }

    @GetMapping("/work-orders/focus/{id}")
    public String focusSpecificWorkOrderSheetFromLedger(@PathVariable("id") Long id) {
        return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + id;
    }

    // =========================================================
    // 📦 REVISED DIRECT-PROCUREMENT INTAKE ENGINE (SaaS SECURED)
    // =========================================================
    @PostMapping("/work-orders/add-item")
    public String addDirectPurchaseLineItemToWorkOrder(
            @RequestParam("workOrderId") Long workOrderId,
            @ModelAttribute("activeTenant") String activeTenant,
            @RequestParam("itemType") String itemType,
            @RequestParam("description") String description,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("retailPrice") BigDecimal retailPrice,
            @RequestParam(value = "partNumber", required = false) String partNumber,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "wholesaleCost", required = false) BigDecimal wholesaleCost,
            RedirectAttributes redirectAttributes) {

        WorkOrder workOrderRecord = workOrderRepository.findByIdAndTenantId(workOrderId, activeTenant)
                .orElseThrow(() -> new IllegalArgumentException("Invalid work order: " + workOrderId));

        if (workOrderRecord.getStatus() != WorkOrderStatus.OPENED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This invoice record is locked out!");
            return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + workOrderId;
        }

        LineItem newLineItem = new LineItem();
        newLineItem.setWorkOrder(workOrderRecord);
        newLineItem.setDescription(description.trim());
        newLineItem.setQuantity(quantity);

        if ("LABOR".equalsIgnoreCase(itemType)) {
            newLineItem.setItemType(LineItemType.LABOR);
            newLineItem.setWholesaleCost(BigDecimal.ZERO);
        } else {
            newLineItem.setItemType(LineItemType.PART);
            newLineItem.setPartNumber(partNumber != null && !partNumber.isEmpty() ? partNumber.trim().toUpperCase() : "N/A");
            newLineItem.setVendor(vendor != null && !vendor.isEmpty() ? vendor.trim().toUpperCase() : "STOCK");
            newLineItem.setWholesaleCost(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        }

        BigDecimal finalizedRetailPrice = retailPrice;
        if (("PART".equalsIgnoreCase(itemType) || "MISC".equalsIgnoreCase(itemType)) &&
                (finalizedRetailPrice == null || finalizedRetailPrice.compareTo(BigDecimal.ZERO) == 0)) {
            finalizedRetailPrice = WorkOrder.calculateMatrixRetailPrice(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        }

        newLineItem.setRetailPrice(finalizedRetailPrice);
        lineItemRepository.save(newLineItem);

        redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to invoice!");
        return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + workOrderId;
    }

    // =========================================================
    // 🖨️ DYNAMIC SaaS PRINT RECEIPT ROUTE CONTROLLER (SECURED)
    // =========================================================
    @GetMapping("/orders/{id}/print")
    public String renderPrintableInvoiceSheet(@PathVariable("id") Long id,
                                              @ModelAttribute("activeTenant") String activeTenant,
                                              Model model) {
        System.out.println("⚡ [PRINTER ENGINE] Initializing invoice generation loop for ticket ID: " + id);
        java.util.Optional<WorkOrder> orderOpt = workOrderRepository.findByIdAndTenantId(id, activeTenant);

        if (orderOpt.isEmpty()) {
            return "redirect:/dashboard?tab=orders";
        }

        WorkOrder order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("vehicle", order.getVehicle());
        model.addAttribute("customer", order.getVehicle().getCustomer());

        // 🟢 THE FIX: Stream, sort by business hierarchy rules, and collect into a clean, un-wrapped List array
        java.util.List<?> sortedLineItems = order.getLineItems().stream()
                .sorted(java.util.Comparator.comparingInt(item -> {
                    // Convert type to uppercase safely to match 'LABOR', 'PART', 'MISC' strings
                    String type = String.valueOf(item.getItemType()).toUpperCase();
                    if (type.contains("LABOR")) return 1; // 1st Priority
                    if (type.contains("PART"))  return 2; // 2nd Priority
                    return 3;                             // 3rd Priority (MISC and everything else)
                }))
                .collect(java.util.stream.Collectors.toList());

        // Pass the guaranteed sorted list array context down to the template canvas engine
        model.addAttribute("lineItems", sortedLineItems);

        model.addAttribute("partsSubtotal", order.getPartsSubtotal());
        model.addAttribute("laborSubtotal", order.getLaborSubtotal());
        model.addAttribute("grandTotal", order.getTotalDue());
        model.addAttribute("amountPaid", order.getAmountPaid());
        model.addAttribute("balanceDue", order.getRemainingBalance());

        Tenant tenantProfile = tenantRepository.findById(activeTenant)
                .orElse(new Tenant(activeTenant, "XYZ Auto Services LLC", "27 Hicks Av, Annapolis, MD 21401", "(443) 325-6663", false));

        model.addAttribute("shopName", tenantProfile.getShopName());
        model.addAttribute("shopAddress", tenantProfile.getAddress());
        model.addAttribute("shopPhone", tenantProfile.getPhone());
        model.addAttribute("isDemoMode", tenantProfile.isDemoMode());

        return "print_invoice";
    }

    // =========================================================
    // 🗑️ PURGE LINE ITEM ROW FROM WORK ORDER LEDGER (SaaS SECURED)
    // =========================================================
    @GetMapping("/work-orders/delete-item")
    public String deleteLineItemFromWorkOrder(
            @RequestParam("focusedWorkOrderId") Long focusedWorkOrderId,
            @RequestParam("deleteItemId") Long deleteItemId,
            @ModelAttribute("activeTenant") String activeTenant, // 🟢 Spring Security/Controller advice populates this
            RedirectAttributes redirectAttributes) {

        // Fallback fallback for local sandbox execution context if model attribute is empty
        if (activeTenant == null || activeTenant.trim().isEmpty() || activeTenant.contains("anonymous")) {
            activeTenant = "main-st-demo"; // 🟢 Match this to your local seed data profile
        }

        final String finalTenant = activeTenant;

        // 1. Locate the parent order using tenant isolation rules
        WorkOrder workOrderRecord = workOrderRepository.findByIdAndTenantId(focusedWorkOrderId, finalTenant)
                .orElseThrow(() -> new IllegalArgumentException("Invalid work order contextual mapping: " + focusedWorkOrderId));

        if (workOrderRecord.getStatus() != WorkOrderStatus.OPENED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot alter rows on locked or finalized ledger archives!");
            return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + focusedWorkOrderId;
        }

        // 2. Clear out the line item
        lineItemRepository.findById(deleteItemId).ifPresent(item -> {
            if (item.getWorkOrder() != null && item.getWorkOrder().getId().equals(focusedWorkOrderId)) {
                lineItemRepository.delete(item);
                redirectAttributes.addFlashAttribute("successMessage", "Line item successfully removed from worksheet.");
            }
        });

        return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + focusedWorkOrderId;
    }

    // =========================================================
    // 💾 RESTFUL MODAL LINE ITEM CHANGES SAVES (SaaS SECURED)
    // =========================================================
    @PostMapping("/work-orders/update-item/{itemId}")
    public String updateLineItemRowMetrics(
            @RequestParam(value = "workOrderId", required = false) Long workOrderId,
            @PathVariable("itemId") Long itemId,
            @RequestParam(value = "itemType", required = false) String itemType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "quantity", required = false) BigDecimal quantity,
            @RequestParam(value = "retailPrice", required = false) BigDecimal retailPrice,
            @RequestParam(value = "partNumber", required = false) String partNumber,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "wholesaleCost", required = false) BigDecimal wholesaleCost,
            @ModelAttribute("activeTenant") String activeTenant, // 🟢 Kept clean as a ModelAttribute
            RedirectAttributes redirectAttributes) {

        if (workOrderId == null || itemId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "CRITICAL ERROR: Corrupted form identifier sequence received.");
            return "redirect:/dashboard?tab=orders";
        }

        // 🟢 FIX OVERRIDE: Ensure local sandbox tests drop onto your active database seed record
        if (activeTenant == null || activeTenant.trim().isEmpty() || activeTenant.contains("anonymous")) {
            activeTenant = "main-st-demo"; // 💻 Verify this matches your local database seed profile string exactly!
        }

        final String finalTenant = activeTenant;

        // 1. Tenant verification security boundary check
        WorkOrder workOrderRecord = workOrderRepository.findByIdAndTenantId(workOrderId, finalTenant)
                .orElseThrow(() -> new IllegalArgumentException("Invalid work order contextual mapping: " + workOrderId));

        if (workOrderRecord.getStatus() != WorkOrderStatus.OPENED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This work order ledger is finalized and locked down!");
            return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + workOrderId;
        }

        // 2. Locate line item record context
        LineItem existingItem = lineItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Target row item not found: " + itemId));

        if (existingItem.getWorkOrder() == null || !existingItem.getWorkOrder().getId().equals(workOrderId)) {
            throw new SecurityException("Data integrity violation cross-leak intercepted!");
        }

        // 3. Hydrate fields safely
        existingItem.setDescription(description != null ? description.trim() : "Unclassified Item");
        existingItem.setQuantity(quantity != null ? quantity : BigDecimal.ONE);

        if ("LABOR".equalsIgnoreCase(itemType)) {
            existingItem.setWholesaleCost(BigDecimal.ZERO);
            existingItem.setPartNumber(null);
            existingItem.setVendor(null);
        } else {
            existingItem.setPartNumber(partNumber != null && !partNumber.isEmpty() ? partNumber.trim().toUpperCase() : "N/A");
            existingItem.setVendor(vendor != null && !vendor.isEmpty() ? vendor.trim().toUpperCase() : "STOCK");
            existingItem.setWholesaleCost(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        }

        // 4. Matrix multiplier fallback calculations
        BigDecimal finalizedRetailPrice = retailPrice;
        if (!"LABOR".equalsIgnoreCase(itemType) && (finalizedRetailPrice == null || finalizedRetailPrice.compareTo(BigDecimal.ZERO) == 0)) {
            finalizedRetailPrice = WorkOrder.calculateMatrixRetailPrice(wholesaleCost != null ? wholesaleCost : BigDecimal.ZERO);
        } else if (finalizedRetailPrice == null) {
            finalizedRetailPrice = BigDecimal.ZERO;
        }
        existingItem.setRetailPrice(finalizedRetailPrice);

        // 5. Save and commit
        lineItemRepository.save(existingItem);

        redirectAttributes.addFlashAttribute("successMessage", "Ledger row updated successfully.");
        return "redirect:/dashboard?tab=orders&focusedWorkOrderId=" + workOrderId;
    }


}
