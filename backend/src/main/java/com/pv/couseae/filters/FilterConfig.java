package com.pv.couseae.filters;

import com.pv.couseae.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * These filters are @Component beans, so Boot also registers them as servlet filters.
 * They must run only inside SecurityFilterChain — otherwise OncePerRequestFilter
 * skips the security-chain copy and SecurityContextHolderFilter wipes auth.
 *
 * File name is FilterConfig (not FilterConflict).
 */
@Configuration
public class FilterConfig {

//    // ✅ Define GatewayAuthFilter bean
//    @Bean
//    public GatewayAuthFilter gatewayAuthFilterBean(SecurityProperties securityProperties,
//                                                   JwtUtill jwtUtill,
//                                                   RedisBlacklistService blacklistService) {
//        return new GatewayAuthFilter(securityProperties, jwtUtill, blacklistService);
//    }
//
//    // ✅ Register GatewayAuthFilter
//    @Bean
//    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilter(GatewayAuthFilter gatewayAuthFilterBean) {
//        FilterRegistrationBean<GatewayAuthFilter> bean = new FilterRegistrationBean<>(gatewayAuthFilterBean);
//        bean.addUrlPatterns("/payouts/ping", "/payouts/createOrder", "/payouts/payinOrderStatus");
//        bean.setOrder(2); // high priority
//        return bean;
//    }
//
//    // ✅ Define IpBoundApiKeyWebFilter bean
//    @Bean
//    public IpBoundApiKeyFilter ipBoundApiKeyWebFilter(SecurityProperties securityProperties,
//            IpApiKeyRepo repo,
//            PasswordEncoder passwordEncoder,
//            IpBoundKeyGenerator keyGen,
//            UserRepoDB userRepo,
//            IPEncryptionService ipEncryptionService) {
//        return new IpBoundApiKeyFilter(repo,passwordEncoder,keyGen, userRepo,ipEncryptionService,securityProperties);
//    }
//
//    // ✅ Register IpBoundApiKeyWebFilter
//    @Bean
//    public FilterRegistrationBean<IpBoundApiKeyFilter> ipBoundApiKeyFilter(IpBoundApiKeyFilter ipBoundApiKeyWebFilter) {
//        FilterRegistrationBean<IpBoundApiKeyFilter> bean = new FilterRegistrationBean<>(ipBoundApiKeyWebFilter);
//        bean.addUrlPatterns("/payouts/ping", "/payouts/createOrder", "/payouts/payinOrderStatus");
//        bean.setOrder(1); // run after GatewayAuthFilter
//        return bean;
//    }

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
