package com.pv.couseae.filters;

import com.pv.couseae.services.RedisBlacklistService;
import com.pv.couseae.utill.JwtUtill;
import com.pv.couseae.utill.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authenticates every protected request. Dashboard calls send Bearer JWT;
 * merchant S2S calls get X-Authenticated-User from IpBoundApiKeyFilter.
 * Downstream code can read either SecurityContext or X-Authenticated-User.
 */
@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final JwtUtill jwtUtil;
    private final RedisBlacklistService blacklistService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public GatewayAuthFilter(SecurityProperties securityProperties,
                             JwtUtill jwtUtil,
                             RedisBlacklistService blacklistService) {
        this.securityProperties = securityProperties;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String contextPath = request.getContextPath();     // /payout_service
        String uri = request.getRequestURI();               // /payout_service/admin/keys/TestIPKey

        String path = uri.substring(contextPath.length()); // /admin/keys/TestIPKey
        log.info("In GatewayAuthFilter ContextPath={}, URI={}, FinalPath={}",  request.getContextPath(),  request.getRequestURI(), path);
        return uri.equals("/favicon.ico")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || securityProperties.getPublicPaths().stream()
                .anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("GatewayAuthFilter path: {}", path);


        if (isAlreadyAuthenticated()) {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();
            filterChain.doFilter(withIdentityHeaders(request, user), response);
            return;
        }

        String jwt = extractBearerToken(request);
        if (jwt != null) {
            if (blacklistService.isBlacklisted(jwt)) {
                unauthorized(response, "Token is blacklisted");
                return;
            }
            try {
                String user = jwtUtil.extractUserName(jwt);
                if (user == null || user.isBlank()) {
                    unauthorized(response, "Invalid token");
                    return;
                }
                setAuthentication(request, user, authoritiesFromJwt(jwt));
                log.info("JWT accepted for user={}", user);
                filterChain.doFilter(withIdentityHeaders(request, user), response);
                return;
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                unauthorized(response, "Invalid or expired token");
                return;
            }
        }

        String user = firstNonBlank(
                request.getHeader("X-Authenticated-User"),
                request.getHeader("x-authenticated-user"));
        log.info("Headers => user={}", user);

        if (user == null || user.isBlank()) {
            log.warn("Missing X-Authenticated-User header and no Bearer token");
            unauthorized(response, "Missing X-Authenticated-User header");
            return;
        }

        setAuthentication(request, user, Collections.emptyList());
        filterChain.doFilter(withIdentityHeaders(request, user), response);
    }

    private boolean isAlreadyAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getName() != null
                && !auth.getName().isBlank();
    }

    private String extractBearerToken(HttpServletRequest request) {
        String auth = headerIgnoreCase(request, "Authorization");
        if (auth == null || auth.isBlank()) {
            return null;
        }
        auth = auth.trim();
        if (auth.length() < 8 || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = auth.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private String headerIgnoreCase(HttpServletRequest request, String name) {
        String direct = request.getHeader(name);
        if (direct != null && !direct.isBlank()) {
            return direct;
        }
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return null;
        }
        while (names.hasMoreElements()) {
            String headerName = names.nextElement();
            if (headerName != null && headerName.equalsIgnoreCase(name)) {
                return request.getHeader(headerName);
            }
        }
        return null;
    }

    private List<GrantedAuthority> authoritiesFromJwt(String jwt) {
        List<String> roleNames = jwtUtil.extractRoles(jwt);
        if (roleNames == null) {
            return Collections.emptyList();
        }
        return roleNames.stream()
                .map(role -> role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private void setAuthentication(HttpServletRequest request,
                                   String user,
                                   List<GrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Ensures every downstream lookup of X-Authenticated-User succeeds after JWT auth.
     * IpBoundApiKeyFilter strips a client-supplied copy on the Bearer path; this puts
     * the verified identity back on the request.
     */
    private HttpServletRequest withIdentityHeaders(HttpServletRequest request, String user) {
        return new HttpServletRequestWrapper(request) {
            private final Map<String, String> customHeaders = new LinkedHashMap<>();

            {
                customHeaders.put("X-Authenticated-User", user);
            }

            private String lookup(String name) {
                if (name == null) {
                    return null;
                }
                for (Map.Entry<String, String> e : customHeaders.entrySet()) {
                    if (e.getKey().equalsIgnoreCase(name)) {
                        return e.getValue();
                    }
                }
                return null;
            }

            @Override
            public String getHeader(String name) {
                String headerValue = lookup(name);
                return headerValue != null ? headerValue : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new LinkedHashSet<>(customHeaders.keySet());
                Enumeration<String> original = super.getHeaderNames();
                while (original.hasMoreElements()) {
                    String n = original.nextElement();
                    if (lookup(n) == null) {
                        names.add(n);
                    }
                }
                return Collections.enumeration(names);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                String headerValue = lookup(name);
                if (headerValue != null) {
                    return Collections.enumeration(List.of(headerValue));
                }
                return super.getHeaders(name);
            }
        };
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private void unauthorized(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{ \"error\": \"" + reason + "\", \"source\": \"Payout-service\" }";
        response.getWriter().write(body);
    }
}
