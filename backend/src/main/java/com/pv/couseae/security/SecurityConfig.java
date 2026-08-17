package com.pv.couseae.security;

import com.pv.couseae.filters.GatewayAuthFilter;
import com.pv.couseae.filters.IpBoundApiKeyFilter;
import com.pv.couseae.handlers.CustomAccessDeniedHandler;
import com.pv.couseae.handlers.CustomAuthenticationEntryPoint;
import com.pv.couseae.services.CustomUserDetailsService;
import com.pv.couseae.utill.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    public SecurityConfig( SecurityProperties securityProperties ) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   GatewayAuthFilter jwtRequestFilter,
                                                   IpBoundApiKeyFilter ipBoundApiKeyFilter,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   CustomAuthenticationEntryPoint authenticationEntryPoint,
                                                   CustomAccessDeniedHandler accessDeniedHandler) throws Exception {

        List<String> publicPaths = securityProperties.getPublicPaths();
        String[] paths = publicPaths.toArray(new String[0]);

        log.info("Public paths: {}", (Object) paths);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(paths).permitAll()
                        .anyRequest().authenticated())
                        .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(ipBoundApiKeyFilter , UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtRequestFilter, IpBoundApiKeyFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       DaoAuthenticationProvider provider) throws Exception {
        return http.getSharedObject(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder.class)
                .authenticationProvider(provider)
                .build();
    }
}