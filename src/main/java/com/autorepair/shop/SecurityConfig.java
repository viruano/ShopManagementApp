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
                .csrf(csrf -> csrf.disable()) // 🔓 Disables CSRF to permit rapid testing form entries
                .authorizeHttpRequests(auth -> auth
                        // 👥 1. Permitted Public Static Assets
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 🖨️ 2. Permitted Print Viewport Mappings
                        .requestMatchers("/orders/*/print").permitAll()

                        // 🔐 3. Permitted Core Security Endpoints (CRITICAL REDIRECT LOOP FIX)
                        .requestMatchers("/login", "/logout", "/error").permitAll()

                        // 👑 4. All other shop floor operations demand strict login authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // 👑 String-shorthand matching bypassing explicit class dependencies
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
