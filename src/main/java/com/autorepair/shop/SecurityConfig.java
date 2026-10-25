package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${XYZ_SHOP_LIVE_USERNAME:admin}")
    private String shopUsername;

    @Value("${XYZ_SHOP_LIVE_PASSWORD:shop2026}")
    private String shopPassword;

    @Value("${DEMO_SHOP_LIVE_USERNAME:demo}")
    private String demoUsername;

    @Value("${DEMO_SHOP_LIVE_PASSWORD:demo2026}")
    private String demoPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // 🔑 SECURE UNIFIED MULTI-USER IDENTITY MATRIX
    // =========================================================
    @Bean
    public UserDetailsService userDetailsService() {
        // 👑 Profile 1: Master Production Admin Account
        UserDetails admin = User.withUsername(shopUsername)
                .password(passwordEncoder().encode(shopPassword))
                .roles("ADMIN")
                .build();

        // 🤝 Profile 2: Sandbox Evaluation Demo Account
        UserDetails demo = User.withUsername(demoUsername)
                .password(passwordEncoder().encode(demoPassword))
                .roles("USER")
                .build();

        // Natively registers both users so they work on localhost and in the cloud out-of-the-box!
        return new InMemoryUserDetailsManager(admin, demo);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabled for fast internal data row submissions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/portfolio", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/portfolio")
                        .permitAll()
                );

        return http.build();
    }
}
