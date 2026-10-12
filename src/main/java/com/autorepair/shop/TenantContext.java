package com.autorepair.shop;

/**
 * 🔒 THREAD-LOCAL SaaS TENANT CONTEXT TRACKER
 * Securely isolates and retains the active shop's Tenant ID string
 * for the duration of the current execution request thread.
 */
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
