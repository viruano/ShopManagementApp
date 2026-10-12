package com.autorepair.shop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔓 Disables CSRF to permit rapid shop form entries
                .authorizeHttpRequests(auth -> auth
                        // 👥 1. Permitted Public Static Asset Patches
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 🖨️ 2. Permitted Print Viewport Mappings (PREVENTS BLANK PRINT TABS)
                        .requestMatchers("/orders/*/print").permitAll()

                        // 👑 3. All other internal ERP shop operations demand strict authentication
                        .anyRequest().authenticated()
                )
                // 🔒 REINSTALLS THE DEFAULT BUILT-IN SPRING SECURITY LOGIN INTERFACE:
                // Generates the login panel layout automatically with NO login.html required!
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}
