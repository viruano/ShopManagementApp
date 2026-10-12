package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🚚 TENANT 1: LIVE PRODUCTION WORKSPACE ANCHORS
    @Value("${SHOP_SECURITY_LIVE_USERNAME:admin}")
    private String shopUsername;

    @Value("${SHOP_SECURITY_LIVE_PASSWORD:shop2026}")
    private String shopPassword;

    // 🚚 TENANT 2: MAIN STREET DEMO WORKSPACE ANCHORS
    @Value("${DEMO_SECURITY_LIVE_USERNAME:demo}")
    private String demoUsername;

    @Value("${DEMO_SECURITY_LIVE_PASSWORD:demo2026}")
    private String demoPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔓 Disables CSRF to permit rapid shop form entries
                .authorizeHttpRequests(auth -> auth
                        // 👥 1. Permitted Public Static Asset Patches
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 🖨️ 2. Permitted Print Viewport Mappings (PREVENTS BLANK TABS)
                        .requestMatchers("/orders/*/print").permitAll()

                        // 👑 3. All other internal ERP shop operations demand strict authentication
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults()) // Uses the auto-generated native prompt window
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    // =========================================================
    // 🔐 ENVIRONMENT-BOUND MULTI-TENANT CREDENTIAL REGISTRY
    // =========================================================
    @Bean
    public UserDetailsService userDetailsService() {
        @SuppressWarnings("deprecation")

        // Profile A: Primary Live Production Account
        UserDetails customShopUser = User.withDefaultPasswordEncoder()
                .username(shopUsername.trim())
                .password(shopPassword.trim())
                .roles("ADMIN")
                .build();

        // Profile B: Secondary Main Street Demo Garage Account
        UserDetails demoShopUser = User.withDefaultPasswordEncoder()
                .username(demoUsername.trim())
                .password(demoPassword.trim())
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(customShopUser, demoShopUser);
    }
}