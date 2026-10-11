package com.autorepair.shop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // 👑 FORCE COMPONENT OVERRIDE: Tells Spring explicitly to use this filter chain bean
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔓 Disables CSRF protection layers to allow standard form submission
                .authorizeHttpRequests(auth -> auth
                        // 👥 1. Permitted Public Static Asset Paths
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 🖨️ 2. Permitted Print Viewport Mappings
                        .requestMatchers("/orders/*/print").permitAll()

                        // 🔐 3. Permitted Core Authentication Gateways (CRITICAL RE-DIRECTION FIX)
                        .requestMatchers("/login", "/logout", "/error").permitAll()

                        // 👑 4. All other shop floor operations require strict login authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // =========================================================
    // 🔐 AGENCY ADMINISTRATOR CREDENTIAL BEAN
    // =========================================================
    @Bean
    public UserDetailsService userDetailsService() {
        @SuppressWarnings("deprecation")
        UserDetails adminUser = User.withDefaultPasswordEncoder() // 👑 FIXED METHOD NAME
                .username("admin")
                .password("shop2026")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(adminUser);
    }

}
