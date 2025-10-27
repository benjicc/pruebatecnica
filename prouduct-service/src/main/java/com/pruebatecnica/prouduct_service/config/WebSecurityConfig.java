package com.pruebatecnica.prouduct_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // Disable X-Frame-Options
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml",
                    "/swagger-resources/**", "/webjars/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable()) // Disable HTTP Basic auth
            .formLogin(form -> form.disable()); // Disable form login
        return http.build();
    }
}