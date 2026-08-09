package com.devconnect.contest.securityAuth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // no/invalid token -> 401, not the default 403
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"))
                        // valid token but wrong role -> 403 (this one stays correct as-is)
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                        // public reads — anyone can browse problems/contests, no login needed
                        .requestMatchers(HttpMethod.GET, "/api/problems/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/contests/**").permitAll()

                        // problem/contest management — admin only
                        .requestMatchers(HttpMethod.POST, "/api/problems/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/problems/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/problems/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contests/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/contests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/contests/**").hasRole("ADMIN")

                        // debug endpoint — admin only (or delete this controller entirely before real launch)
                        .requestMatchers("/api/judge-test/**").hasRole("ADMIN").requestMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()

                        // submissions — must be logged in, any role
                        .requestMatchers("/api/submissions/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}