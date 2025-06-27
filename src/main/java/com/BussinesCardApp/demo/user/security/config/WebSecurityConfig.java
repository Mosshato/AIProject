package com.BussinesCardApp.demo.user.security.config;

import com.BussinesCardApp.demo.user.authentication.jwt.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/pdf/history").authenticated()
                        .requestMatchers(HttpMethod.GET, "/myaccount").authenticated()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v*/registration").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v*/registration/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pdf/download/**").authenticated()
                        .requestMatchers("/pdf/generate").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1) Put your React laptop’s LAN origin here, e.g. "http://10.231.22.55:3000"
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://192.168.0.104:3000"
        ));
        // 2) HTTP methods your frontend uses
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3) Allow any headers
        config.setAllowedHeaders(List.of("*"));

        // 4) If you plan to send credentials (cookies/Basic auth), keep this true
        config.setAllowCredentials(true);

        // 5) How long to cache preflight response (1 hour)
        config.setMaxAge(3600L);

        // 6) Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

