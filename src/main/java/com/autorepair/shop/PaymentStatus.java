package com.autorepair.shop;

public enum PaymentStatus {
    UNPAID,         // 🛑 Outstanding balance, zero collections posted
    PARTIALLY_PAID, // ⚠️ Counter deposit or baseline insurance payout logged
    FULLY_PAID      // ✅ Ledger zeroed out completely
}
