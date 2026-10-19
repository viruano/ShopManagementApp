package com.autorepair.shop;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1) // 🥇 Fires early in the servlet request lifecycle to catch redirects instantly
public class RedirectRefactorFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 📡 Wrap the native response so we can intercept outgoing "Location" redirect headers
        HttpServletResponseWrapper redirectInterceptor = new HttpServletResponseWrapper(httpResponse) {
            @Override
            public void sendRedirect(String location) throws IOException {
                String host = httpRequest.getHeader("Host");
                if (host != null) {
                    host = host.toLowerCase();
                }

                // 🚗 IF the user is on the shop subdomain and the app tries to redirect to root:
                if ((host != null && (host.startsWith("shopapp.") || host.contains("shopmanagementapp-production")))
                        && (location.equals("/") || location.startsWith("/?"))) {

                    // Rewrite the redirect destination URL natively to preserve the session dashboard state!
                    String secureDashboardLocation = location.replace("/", "/dashboard");
                    System.out.println("⚡ [REDIRECT REFACTOR] Intercepted loop! Rewriting " + location + " ➔ " + secureDashboardLocation);
                    super.sendRedirect(secureDashboardLocation);
                    return;
                }

                super.sendRedirect(location);
            }
        };

        chain.doFilter(request, redirectInterceptor);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
