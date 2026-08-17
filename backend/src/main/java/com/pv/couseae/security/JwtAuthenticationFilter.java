package com.pv.couseae.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.services.RedisBlacklistService;
import com.pv.couseae.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
//    private final TokenBlacklist blacklist;
    private final RedisBlacklistService blacklistService;



    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String apiReqFwdUser = request.getHeader("X-Authenticated-User");
        log.info("Url -> "+request.getMethod()+" " +request.getRequestURI()+", Gateway User -> "+apiReqFwdUser);

        final String jwt;
        final String userEmail;
        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && !request.getRequestURI().contains("v1/")) {
            filterChain.doFilter(request, response);
            return;
        } else if (request.getRequestURI().contains("v1/")) {
            String merchantAppId = request.getHeader("merchantAppId");
            String merchantSecretId = request.getHeader("merchantSecretId");
            System.out.println("MerchantModel");
            if (merchantAppId == null || merchantAppId.isEmpty() ||merchantSecretId ==null || merchantSecretId.isEmpty()) {
                filterError("Authentication fail", response, filterChain, request);
                return;
            }
            UserDetails userDetails = userService.loadUserByAppIdAndSecretKey(merchantAppId, merchantSecretId);
            if (userDetails == null) {
                filterError("Authentication fail, Invalid Keys", response, filterChain, request);
                return;
            }
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
        } else {

            jwt = authHeader.substring(7);
            log.info("JWT -> " + jwt);
            log.info("blacklist -> " + blacklistService.toString());

            if (blacklistService.isBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            userEmail = jwtService.extractUserName(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.userDetailsService()
                        .loadUserByUsername(userEmail);

                // ✅ Validate signature + expiry + subject match
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // ✅ Extract roles from JWT claims
                    List<String> roles = jwtService.extractRoles(jwt);
                    log.info("JWT roles -> " + roles.toString());
                    Set<String> jwtRoles = new HashSet<>(roles);
                    Collection<? extends GrantedAuthority> dbAuthorities = userDetails.getAuthorities();
                    Set<String> dbRoles = dbAuthorities.stream()
                            .map(GrantedAuthority::getAuthority) // convert to String
                            .collect(Collectors.toSet());


                   log.info("DB roles -> " + dbRoles.toString());
                  // boolean roleMatch = dbRoles.containsAll(jwtRoles);
                    boolean roleMatch = jwtRoles.stream()
                            .map(role -> role.replace("ROLE_", ""))   // Remove prefix
                            .anyMatch(dbRoles::contains);
                    log.info("roleMatch -> " + roleMatch);
                    if (!roleMatch) {
//                    if (!roleMatch && false) {
//                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
//                                "Role mismatch - invalid token usage");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Role mismatch - invalid token usage");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, dbAuthorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);

                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired or invalid");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void filterError(String message, HttpServletResponse response, FilterChain filterChain, HttpServletRequest request) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("source", "main-service");
        errorResponse.put("status", "fail");
        errorResponse.put("message", message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
        filterChain.doFilter(request, response);
    }
}
