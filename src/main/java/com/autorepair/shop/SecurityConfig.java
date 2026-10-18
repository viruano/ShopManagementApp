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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TenantFilter tenantFilter;

    public SecurityConfig(TenantFilter tenantFilter) {
        this.tenantFilter = tenantFilter;
    }

    // 🚚 ALIGNED DYNAMIC SHOP INJECTION ANCHORS:
    // Connects seamlessly to your newly configured Railway variable layout names
    @Value("${XYZ_SHOP_LIVE_USERNAME:admin}")
    private String shopUsername;

    @Value("${XYZ_SHOP_LIVE_PASSWORD:shop2026}")
    private String shopPassword;

    @Value("${DEMO_SHOP_LIVE_USERNAME:demo}")
    private String demoUsername;

    @Value("${DEMO_SHOP_LIVE_PASSWORD:demo2026}")
    private String demoPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔓 Disables CSRF to permit rapid shop form entries
                .authorizeHttpRequests(auth -> auth
                        // 👑 WHITELIST CONTEXT PORTS:
                        // Added "/portfolio" to allow public client browsing with zero login screens required!
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/orders/*/print").permitAll()
                        .requestMatchers("/portfolio").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.permitAll())
                .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        @SuppressWarnings("deprecation")
        UserDetails customShopUser = User.withDefaultPasswordEncoder()
                .username(shopUsername.trim())
                .password(shopPassword.trim())
                .roles("ADMIN")
                .build();

        UserDetails demoShopUser = User.withDefaultPasswordEncoder()
                .username(demoUsername.trim())
                .password(demoPassword.trim())
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(customShopUser, demoShopUser);
    }
}
