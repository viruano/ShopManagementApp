package com.autorepair.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${shop.config.demo-mode:false}")
    private boolean isDemoMode;

    // Inject XYZ Auto Live Shop Credentials
    @Value("${shop.security.live.username}")
    private String liveUsername;
    @Value("${shop.security.live.password}")
    private String livePassword;

    // Inject Demo Shop Credentials
    @Value("${shop.security.demo.username}")
    private String demoUsername;
    @Value("${shop.security.demo.password}")
    private String demoPassword;

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String targetUsername;
        String targetPassword;
        String targetRole;

        // 🛡️ DYNAMIC ACCOUNT AUTHENTICATION BALANCER
        if (isDemoMode) {
            targetUsername = demoUsername;
            targetPassword = encoder.encode(demoPassword);
            targetRole = "DEMO_ADVISOR";
        } else {
            targetUsername = liveUsername;
            targetPassword = encoder.encode(livePassword);
            targetRole = "SERVICE_ADVISOR";
        }

        UserDetails user = User.withUsername(targetUsername)
                .password(targetPassword)
                .roles(targetRole)
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabled for local development line item post edits
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .permitAll()
                )
                .logout(logout -> logout
                        .permitAll()
                );
        return http.build();
    }
}
