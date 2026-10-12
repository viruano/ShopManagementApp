package com.autorepair.shop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔓 Disables CSRF to allow smooth shop data entry
                .authorizeHttpRequests(auth -> auth
                        // 👑 GLOBAL OPEN ACCESS PASS-THROUGH:
                        // Permits all users to access the dashboard, print layouts, and forms with NO login required!
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
