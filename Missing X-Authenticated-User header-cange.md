# Missing `X-Authenticated-User` header — code change only

Error:

```json
{
    "error": "Missing X-Authenticated-User header",
    "source": "Payout-service"
}
```

This JSON is written only in `GatewayAuthFilter.unauthorized(...)`.

This file lists **only** what we added/changed for that error. It does **not** include CORS, `SearchRequest`, Mongo seed, or `merchantAppId` / ZIPAPIKEY work.

---

## Cause

1. Dashboard calls send `Authorization: Bearer <jwt>` (no gateway header).
2. `IpBoundApiKeyFilter` sees Bearer, **strips** `X-Authenticated-User` (anti-spoof). That behaviour was **not** removed.
3. Old `GatewayAuthFilter` then required `X-Authenticated-User` and returned 401. `JwtUtill` was injected and unused.

We did **not** change `IpBoundApiKeyFilter` for this error. We taught `GatewayAuthFilter` to authenticate JWT and put `X-Authenticated-User` back from the token `sub`.

---

## Files touched for this error

| File | Role |
|------|------|
| `backend/src/main/java/com/pv/couseae/filters/GatewayAuthFilter.java` | Main fix |
| `backend/src/main/java/com/pv/couseae/filters/FilterConfig.java` | So this filter runs only in `SecurityFilterChain` (not twice as a servlet filter) |

---

## 1. `GatewayAuthFilter.java`

### Removed

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String path = request.getRequestURI();
    log.info("GatewayAuthFilter path: {}", path);

    String user = request.getHeader("X-Authenticated-User");
    String merchantId = request.getHeader("X-Merchant-Id");
    String roles = request.getHeader("X-Roles");

    log.info("Headers => user={}, merchantId={}, roles={}", user, merchantId, roles);

    if (user == null || user.isBlank()) {
        log.warn("Missing X-Authenticated-User header");
        unauthorized(response, "Missing X-Authenticated-User header");
        return;
    }

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, null, null);

    authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
}
```

Also removed unused `//@Order(2)`.

### Added

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    log.info("GatewayAuthFilter path: {}", request.getRequestURI());

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
```

`unauthorized(...)` was kept (same JSON `source: Payout-service`).

New imports for this file:

```java
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
```

---

## 2. `FilterConfig.java`

Without this, `@Component` registers `GatewayAuthFilter` as a **servlet** filter as well. It can run once outside the security chain; `SecurityContextHolderFilter` then starts an empty context, and APIs still fail.

### Removed (commented payout-only URL registration)

```java
//    @Bean
//    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilter(GatewayAuthFilter gatewayAuthFilterBean) {
//        FilterRegistrationBean<GatewayAuthFilter> bean = new FilterRegistrationBean<>(gatewayAuthFilterBean);
//        bean.addUrlPatterns("/payouts/ping", "/payouts/createOrder", "/payouts/payinOrderStatus");
//        bean.setOrder(2);
//        return bean;
//    }
```

(`IpBoundApiKeyFilter` / `JwtAuthenticationFilter` registrations were disabled in the same class for the same double-filter reason.)

### Added

```java
@Bean
public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilterRegistration(GatewayAuthFilter filter) {
    FilterRegistrationBean<GatewayAuthFilter> bean = new FilterRegistrationBean<>(filter);
    bean.setEnabled(false);
    return bean;
}
```

---

## What we did **not** change for this error

- `IpBoundApiKeyFilter.stripGrantedHeaders()` — still strips client `X-Authenticated-User` on Bearer. Correct.
- `SecurityConfig.java` — still `IpBoundApiKeyFilter` then `GatewayAuthFilter`.
- Sending `X-Authenticated-User` from Postman with Bearer — not required; JWT is enough.

---

## How to call after this change

```
POST http://localhost:8085/<any-protected-api>
Authorization: Bearer <jwt>
```

Do not add `X-Authenticated-User`.  
`https://api.courseraeducation.com` still has the old filter until this build is deployed.
