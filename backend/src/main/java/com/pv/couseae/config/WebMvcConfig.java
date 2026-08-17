package com.pv.couseae.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Applies the dev-secret check to every /payouts/** endpoint by default.
 *
 * New endpoints added to PayoutController are protected automatically — that is the point.
 * Only add exclusions here, deliberately, and never for anything that moves money.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DevSecretInterceptor devSecretInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(devSecretInterceptor)
                .addPathPatterns("/payouts/**")
                .excludePathPatterns(
                        "/payouts/ping",
                        // Swagger / actuator, if they sit under a different base path remove these
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
    }
}