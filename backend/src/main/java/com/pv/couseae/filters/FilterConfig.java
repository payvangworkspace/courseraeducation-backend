package com.pv.couseae.filters;

import com.pv.couseae.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * These filters are @Component beans, so Boot also registers them as servlet filters.
 * They must run only inside SecurityFilterChain — otherwise OncePerRequestFilter
 * skips the security-chain copy and SecurityContextHolderFilter wipes auth.
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilterRegistration(GatewayAuthFilter filter) {
        FilterRegistrationBean<GatewayAuthFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<IpBoundApiKeyFilter> ipBoundApiKeyFilterRegistration(IpBoundApiKeyFilter filter) {
        FilterRegistrationBean<IpBoundApiKeyFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }
}
