// =========================================================
// 📑 CENTRAL WORK ORDER INTAKE MODAL WINDOW CONTROLLERS
// =========================================================
function openNewWorkOrderIntakeModal() {
    const modal = document.getElementById('newWorkOrderIntakeModalWindow');
    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function closeNewWorkOrderIntakeModal() {
    const modal = document.getElementById('newWorkOrderIntakeModalWindow');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }

    // Clear out search input parameters gracefully on close outs
    const inputField = document.getElementById('modalVehicleLookupSearchField');
    if (inputField) inputField.value = "";

    const searchableRows = document.querySelectorAll('.modal-vehicle-searchable-row');
    searchableRows.forEach(row => row.style.display = "");

    const emptyStateCard = document.getElementById('modalSearchEmptyStateRowCard');
    if (emptyStateCard) emptyStateCard.classList.add('hidden');
}

// =========================================================
// 🔍 HIGH-SPEED LIVE IN-MODAL SEARCH MATCHING MATRIX
// =========================================================
function filterModalVehicleDeckRowsLive() {
    // Grab the query tokens and cast uniformly to uppercase
    const searchInput = document.getElementById('modalVehicleLookupSearchField');
    if (!searchInput) return;
    const filterKeyword = searchInput.value.trim().toUpperCase();

    // Isolate every single searchable vehicle row container element inside the modal
    const searchableRows = document.querySelectorAll('.modal-vehicle-searchable-row');
    let visibleRowCount = 0;

    searchableRows.forEach(row => {
        // Pull the comprehensive text blob string from the row's data-search attribute
        const searchMetadataBlob = row.getAttribute('data-search') || '';

        if (searchMetadataBlob.includes(filterKeyword)) {
            row.style.display = ""; // Match: reveal layout row instantly
            visibleRowCount++;
        } else {
            row.style.display = "none"; // Miss: conceal layout row from window
        }
    });

    // Toggle the empty state card message if zero results match the keywords
    const emptyStateCard = document.getElementById('modalSearchEmptyStateRowCard');
    if (emptyStateCard) {
        if (visibleRowCount === 0 && filterKeyword.length > 0) {
            emptyStateCard.classList.remove('hidden');
        } else {
            emptyStateCard.classList.add('hidden');
        }
    }
}
