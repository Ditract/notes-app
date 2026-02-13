package com.sanez.security.config;

import com.sanez.security.jwt.AuthEntryPointJwt;
import com.sanez.security.jwt.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler, AuthTokenFilter authTokenFilter,
                          UserDetailsService userDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.authTokenFilter = authTokenFilter;
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(authProvider));
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/notas/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/perfiles/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
