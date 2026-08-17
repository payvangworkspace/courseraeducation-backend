package com.pv.couseae.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Centralised replacement for the per-controller-method "mySecretDev" header check.
 *
 * Why this exists:
 *  - Seven payout endpoints previously had NO auth check at all (batch payout, Canara batch,
 *    RBL balance, etc.) because the check had to be pasted into each method by hand.
 *  - The inline checks returned ResponseEntity.ok(HttpStatus.UNAUTHORIZED) — HTTP 200 with the
 *    body "UNAUTHORIZED" — so clients checking status codes saw a rejection as a success.
 *  - The secret was hardcoded in source and compared with String.equals (not constant time).
 *
 * Configure in application.yml:
 *   app:
 *     dev:
 *       secret: ${DEV_API_SECRET}
 *
 * ROTATE the old value ("Dev123498765") — it is in git history.
 */
@Slf4j
@Component
public class DevSecretInterceptor implements HandlerInterceptor {

    private static final String HEADER = "mySecretDev";

    private final byte[] expected;

    public DevSecretInterceptor(@Value("${app.dev.secret}") String devSecret) {
        if (devSecret == null || devSecret.isBlank()) {
            throw new IllegalStateException("app.dev.secret is not configured");
        }
        this.expected = devSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String provided = request.getHeader(HEADER);

        boolean ok = provided != null
                && MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8), expected);

        if (!ok) {
            log.warn("Rejected unauthenticated request: {} {} from {}",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            // Adjust this body to match your ResponseModel envelope shape.
            response.getWriter().write("{\"status\":false,\"message\":\"Unauthorized\",\"data\":null}");
            return false;
        }

        return true;
    }
}