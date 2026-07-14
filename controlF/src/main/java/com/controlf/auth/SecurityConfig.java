package com.controlf.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
        "/",
        "/index.html",
        "/assets/**",
        "/favicon.ico"
    ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/leyes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/politicos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/validacion/**").hasAnyRole("VALIDADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/leyes/*/import-voting-detail").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/leyes/*/comentarios", "/api/leyes/*/calificaciones", "/api/politicos/*/comentarios", "/api/politicos/*/calificaciones").hasAnyRole("CIUDADANO", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/leyes/*/categoria", "/api/leyes/*/estado", "/api/leyes/*/votos/*/asistencia", "/api/politicos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/politicos/comentarios/*", "/api/leyes/comentarios/*")
    .hasAnyRole("CIUDADANO", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/politicos/comentarios/*", "/api/leyes/comentarios/*")
    .hasAnyRole("CIUDADANO", "ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
