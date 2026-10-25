// =========================================================
// 📈 CORE WORKSHOP UI LAYOUT NAVIGATION & INTERFACING LINKS
// =========================================================
function switchTab(tabId) {
    const sections = ['customers', 'vehicles', 'orders'];
    sections.forEach(s => document.getElementById('section-' + s).classList.add('hidden'));
    document.getElementById('section-' + tabId).classList.remove('hidden');

    const btns = ['customers', 'vehicles', 'orders'];
    btns.forEach(b => document.getElementById('tab-btn-' + b).className = "py-2 px-4 border-b-2 font-bold text-sm transition-all duration-150 border-transparent text-gray-500 hover:text-gray-700");

    const activeColor = tabId === 'vehicles' ? 'border-emerald-600 text-emerald-600' : 'border-indigo-600 text-indigo-600';
    document.getElementById('tab-btn-' + tabId).className = "py-2 px-4 border-b-2 font-bold text-sm transition-all duration-150 " + activeColor;
}

function toggleRegistrationForm() {
    const pane = document.getElementById('registrationFormPane');
    const grid = document.getElementById('clientIndexGridTable');
    if (pane.classList.contains('hidden')) {
        pane.classList.remove('hidden');
        grid.classList.remove('lg:col-span-3');
        grid.classList.add('lg:col-span-2');
    } else {
        pane.classList.add('hidden');
        grid.classList.remove('lg:col-span-2');
        grid.classList.add('lg:col-span-3');
    }
}

function toggleVehicleRegistrationForm() {
    const pane = document.getElementById('vehicleRegistrationFormPane');
    const grid = document.getElementById('vehicleFleetGridTable');
    if (pane.classList.contains('hidden')) {
        pane.classList.remove('hidden');
        grid.classList.remove('lg:col-span-3');
        grid.classList.add('lg:col-span-2');
    } else {
        pane.classList.add('hidden');
        grid.classList.remove('lg:col-span-2');
        grid.classList.add('lg:col-span-3');
    }
}

function applyLaborGuideSelection() {
    const selector = document.getElementById('laborGuideSelector');
    const selectedOption = selector.options[selector.selectedIndex];

    if (selectedOption.value) {
        const rawText = selectedOption.text;
        const descriptionPart = rawText.split(']').pop().split('—').shift().trim();
        const bookHours = selectedOption.value;

        document.getElementById('formItemType').value = 'LABOR';
        document.getElementById('formQuantity').value = bookHours;
        document.getElementById('formDescription').value = descriptionPart;

        const priceField = document.getElementById('formRetailPrice');
        if (!priceField.value || priceField.value === "0.00") {
            priceField.value = "120.00";
        }
    }
}

function formatPhoneNumber(value) {
    if (!value) return value;
    const phoneNumber = value.replace(/\D/g, '');
    const phoneNumberLength = phoneNumber.length;
    if (phoneNumberLength < 4) return phoneNumber;
    if (phoneNumberLength < 7) {
        return `(${phoneNumber.slice(0, 3)}) ${phoneNumber.slice(3)}`;
    }
    return `(${phoneNumber.slice(0, 3)}) ${phoneNumber.slice(3, 6)}-${phoneNumber.slice(6, 10)}`;
}

document.addEventListener('input', function(e) {
    if (e.target && e.target.classList.contains('phone-mask')) {
        const input = e.target;
        const selectionStart = input.selectionStart;
        const originalLength = input.value.length;
        input.value = formatPhoneNumber(input.value);
        const newLength = input.value.length;
        input.setSelectionRange(selectionStart + (newLength - originalLength), selectionStart + (newLength - originalLength));
    }
});

window.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const activeTab = urlParams.get('tab');
    if (activeTab) { switchTab(activeTab); }
});

// =========================================================
// 📈 SHOP METRICS DRAWER DISPLAY INTERFACE ACCORDION HOOKS
// =========================================================
function toggleShopAnalyticsMetrics() {
    const drawer = document.getElementById('shopAnalyticsMetricsDrawer');
    if (drawer) {
        drawer.classList.toggle('hidden');
    }
}

// =========================================================
// 📦 DIRECT-PROCUREMENT MODAL FORM DISPLAY INTERACTION LINKS
// =========================================================
function toggleLineItemFormFieldsBasedOnType() {
    const typeSelector = document.getElementById('formItemType');
    const procurementGroup = document.getElementById('partsProcurementFormFieldsSubGroup');
    const qtyLabel = document.getElementById('formQuantityFieldTitleLabel');

    if (!typeSelector || !procurementGroup || !qtyLabel) return;

    if (typeSelector.value === 'LABOR') {
        // Hide wholesaler cost parameters for mechanical labor entries
        procurementGroup.style.display = "none";
        qtyLabel.innerText = "Labor Book Hours:";

        // Reset parts fields to clean defaults
        document.getElementById('formPartNumber').value = "";
        document.getElementById('formVendor').value = "";
        document.getElementById('formWholesaleCost').value = "0.00";
    } else {
        // Expose procurement data subfields for direct part purchases
        procurementGroup.style.display = "grid";
        qtyLabel.innerText = "Part Qty Count:";
    }
}

function openLineItemModal() {
    const modal = document.getElementById('addLineItemModalWindow');
    if (modal) {
        modal.classList.remove('hidden');
        toggleLineItemFormFieldsBasedOnType(); // Validate form status on open
    }
}

function closeLineItemModal() {
    const modal = document.getElementById('addLineItemModalWindow');
    if (modal) modal.classList.add('hidden');
}

// =========================================================
// 🧮 FRONTEND SLIDING CLIENT MATRIX PRICE PREVIEW
// =========================================================
function runInlineMatrixPartsPriceCalc() {
    const costField = document.getElementById('partsCardWholesaleCostField');
    const retailField = document.getElementById('partsCardRetailPriceField');

    if (!costField || !retailField) return;

    const wholesaleCost = parseFloat(costField.value) || 0;
    let multiplier = 1.30; // Default Tier 5 standard fallback

    if (wholesaleCost <= 10.00) {
        multiplier = 3.00;
    } else if (wholesaleCost <= 50.00) {
        multiplier = 2.00;
    } else if (wholesaleCost <= 100.00) {
        multiplier = 1.60;
    } else if (wholesaleCost <= 300.00) {
        multiplier = 1.45;
    }

    const calculatedRetail = wholesaleCost * multiplier;
    retailField.value = calculatedRetail.toFixed(2);
}

// =========================================================
// 👥 SaaS SECURED ACTIONS MODAL INTERFACES
// =========================================================
function openCustomerEditModal(customerId) {
    console.log("⚡ [CRM ENGINE] Fetching client parameters for profile ID: " + customerId);
    if (!customerId) return;
    window.location.href = "/dashboard?tab=customers&editCustomerId=" + customerId;
}

function openVehicleEditModal(vehicleId) {
    console.log("⚡ [FLEET ENGINE] Fetching fleet specs for profile ID: " + vehicleId);
    if (!vehicleId) return;
    window.location.href = "/dashboard?tab=vehicles&editVehicleId=" + vehicleId;
}
