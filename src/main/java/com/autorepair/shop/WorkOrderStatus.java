package com.autorepair.shop;

public enum WorkOrderStatus {
    OPENED,    // 🔧 Active job active on the shop floor
    COMPLETED, // 🏁 Repairs finished, vehicle staging for collection
    ARCHIVED   // 🔒 Balance settled, paperwork frozen & filed to history log
}
