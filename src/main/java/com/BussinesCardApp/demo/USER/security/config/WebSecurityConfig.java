package com.BussinesCardApp.demo.USER.security.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    /**
     * Definim lanțul de filtre de securitate.
     * Spring va invoca acest bean pentru a construi SecurityFilterChain-ul.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // allow registration endpoints
                        .requestMatchers("/api/v*/registration/**").permitAll()
                        // allow your PDF-generation endpoint
                        .requestMatchers("/pdf/generate").permitAll()
                        // only ADMIN on /admin/**
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // everything else needs login
                        .anyRequest().authenticated()
                )

                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
