package com.doctorqueue.doctorqueue.config;

import com.doctorqueue.doctorqueue.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http

                .csrf(csrf ->
                        csrf.disable()
                )

                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth

                        // Authentication
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // Clinics
                        .requestMatchers(
                                "/api/clinics/**"
                        ).permitAll()

                        // Doctors
                        .requestMatchers(
                                "/api/doctors/**"
                        ).permitAll()

                        // SSE
                        .requestMatchers(
                                "/api/queue/events"
                        ).permitAll()

                        // Everything else
                        .anyRequest()
                        .authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}