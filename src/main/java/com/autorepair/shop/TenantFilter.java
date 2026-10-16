package com.autorepair.shop;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantToken = null;

        // 🛡️ APPROACH A: CHECK FOR DYNAMIC INCOMING SUBDOMAIN / HEADER SIGNALS
        if (httpRequest.getHeader("X-Tenant-ID") != null) {
            tenantToken = httpRequest.getHeader("X-Tenant-ID").trim();
        }

        // 👑 APPROACH B: THE SAAS USER AUTOMATED PRINCIPAL RESOLVER GATES
        // If no header token exists, look at who is physically signed into Spring Security!
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            String username = auth.getName().toLowerCase();

            // 🔒 AUTOMATED ACCOUNT WORKSPACE MATRICES MATCHING:
            if (username.contains("demo") || username.contains("main-st")) {
                tenantToken = "main-st-demo";
            } else {
                tenantToken = "xyz-auto"; // Primary production shop context default anchor
            }
        }

        // Fallback default catch block to guarantee layout rendering stability
        if (tenantToken == null || tenantToken.trim().isEmpty()) {
            tenantToken = "xyz-auto";
        }

        try {
            // Bind the identified string safely right onto the thread memory frame context
            TenantContext.setCurrentTenant(tenantToken);
            chain.doFilter(request, response);
        } finally {
            // Always sweep memory scopes clear to prevent thread pooling memory leak pollution
            TenantContext.clear();
        }
    }
}
