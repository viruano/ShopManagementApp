package com.autorepair.shop;

import jakarta.servlet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    // Inject your live dashboard user parameters directly into your switcher logic
    @Value("${DEMO_SHOP_LIVE_USERNAME:demo}")
    private String targetDemoUser;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String loggedInUsername = authentication.getName();

            // 👑 FLEXIBLE SaaS WORKSPACE DETERMINISTIC ENGINE:
            // Intelligently cross-references your live dashboard parameter variable names
            if (targetDemoUser.trim().equalsIgnoreCase(loggedInUsername)) {
                TenantContext.setCurrentTenant("main-st-demo");
            } else {
                TenantContext.setCurrentTenant("xyz-auto");
            }
        } else {
            TenantContext.setCurrentTenant("xyz-auto");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // 🧼 Prevent multi-thread memory leakage cascades
        }
    }
}
