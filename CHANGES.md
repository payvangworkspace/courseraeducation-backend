# Local run changes (CourseAE backend)

This document lists **what was added, removed, and changed**, including the **actual code**.

Date: 17 Aug 2026  
Profile: **uat** (`application.yml` → `spring.profiles.active: uat`)  
Local URL: `http://localhost:8085` (not `https://api.courseraeducation.com`)

---

## Why

| Client | Error | Cause |
|--------|--------|--------|
| Bearer JWT | `Missing X-Authenticated-User header` | `GatewayAuthFilter` only checked that header. IP-key filter **strips** it when Bearer is present. |
| Payin headers | `Missing API key...` | Filter required `ZIPAPIKEY` before `PayinController`. |
| Postman `Origin` | `Full authentication is required` | CORS `allowCredentials=true` + `allowedOrigins=*`. |
| List body | Unrecognized field `length` | DTO had `size` / `keyword`, UI sends `length` / `search`. |
| After auth | `User.getRole()` NPE | `admin@courseae.com` missing in UAT Mongo. |

---

## 1. `GatewayAuthFilter.java`

Path: `backend/src/main/java/com/pv/couseae/filters/GatewayAuthFilter.java`

### Removed (old `doFilterInternal` — header only, JWT unused)

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

### Added (JWT + header + wrap `X-Authenticated-User`)

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

private HttpServletRequest withIdentityHeaders(HttpServletRequest request, String user) {
    return new HttpServletRequestWrapper(request) {
        private final Map<String, String> customHeaders = new LinkedHashMap<>();
        {
            customHeaders.put("X-Authenticated-User", user);
        }
        // getHeader / getHeaders / getHeaderNames override customHeaders
        // (same pattern as IpBoundApiKeyFilter wrapper)
    };
}
```

New imports added: `HttpServletRequestWrapper`, `AnonymousAuthenticationToken`, `Authentication`, `GrantedAuthority`, `SimpleGrantedAuthority`, `Enumeration`, `LinkedHashMap`, `LinkedHashSet`, `List`, `Map`, `Set`, `Collectors`.

---

## 2. `IpBoundApiKeyFilter.java`

Path: `backend/src/main/java/com/pv/couseae/filters/IpBoundApiKeyFilter.java`

### Added — merchant `appId` / `secret` path (after Bearer skip)

```java
String merchantAppId = getHeaderIgnoreCase(req, "merchantAppId");
String merchantSecretId = getHeaderIgnoreCase(req, "merchantSecretId");
if (merchantAppId != null && !merchantAppId.isBlank()
        && merchantSecretId != null && !merchantSecretId.isBlank()) {
    CachedBodyHttpServletRequest cachedReq = new CachedBodyHttpServletRequest(req);
    User merchant = userRepo.findByAppKeyAndSecretKey(
            merchantAppId.trim(), merchantSecretId.trim());
    if (merchant == null || merchant.getUserId() == null || merchant.getUserId().isBlank()) {
        unauthorized(res, "Invalid Merchant Keys...");
        return;
    }

    String body = cachedReq.getBodyAsString();
    if (!body.isBlank()) {
        JsonNode node = objectMapper.readTree(body);
        if (node.has("merchantId") || node.has("merchnatId")) {
            String bodyMerchantId = node.has("merchantId")
                    ? node.path("merchantId").asText(null)
                    : node.path("merchnatId").asText(null);
            if (bodyMerchantId != null
                    && !bodyMerchantId.equalsIgnoreCase(merchant.getUserId())) {
                unauthorized(res, "Merchant mismatch between keys and request body");
                return;
            }
        }
    }

    log.info("Merchant app/secret accepted — skipping ZIPAPIKEY for path {}", path);
    chain.doFilter(
            wrapGrantedHeaders(cachedReq, extractClientIp(req), merchant.getUserId()),
            res);
    return;
}
```

### Removed — inline ZIPAPIKEY wrapper (replaced by helper)

```java
HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(cachedReq) {
    private final Map<String, String> customHeaders = new LinkedHashMap<>();
    {
        customHeaders.put("X-Client-IP", clientIp);
        customHeaders.put("X-Validated-Merchant-Id", entity.getMerchantId());
        customHeaders.put("X-Authenticated-User", entity.getMerchantId());
    }
    // getHeader / getHeaderNames / getHeaders ...
};
chain.doFilter(wrappedRequest, res);
```

### Added — helper used by ZIPAPIKEY and merchant-key success

```java
private HttpServletRequest wrapGrantedHeaders(HttpServletRequest req, String clientIp, String merchantId) {
    return new HttpServletRequestWrapper(req) {
        private final Map<String, String> customHeaders = new LinkedHashMap<>();
        {
            customHeaders.put("X-Client-IP", clientIp);
            customHeaders.put("X-Validated-Merchant-Id", merchantId);
            customHeaders.put("X-Authenticated-User", merchantId);
        }
        // getHeader / getHeaderNames / getHeaders (same as old inline wrapper)
    };
}
```

ZIPAPIKEY success is now:

```java
chain.doFilter(wrapGrantedHeaders(cachedReq, clientIp, entity.getMerchantId()), res);
```

Bearer skip (`stripGrantedHeaders`) was **not** removed.

---

## 3. `UserRepoDB.java`

Path: `backend/src/main/java/com/pv/couseae/repos/UserRepoDB.java`

### Removed

Nothing except remaining unused comment `//User findByUserId`.

### Added

```java
User findByAppKeyAndSecretKey(String appKey, String secretKey);
```

Full file after change:

```java
public interface UserRepoDB extends MongoRepository<User, String> {
    Optional<User> findByUserId(String username);

    User findByAppKeyAndSecretKey(String appKey, String secretKey);
}
```

---

## 4. `FilterConfig.java`

Path: `backend/src/main/java/com/pv/couseae/filters/FilterConfig.java`

### Removed (commented stubs that registered only payout URLs)

```java
//    @Bean
//    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilter(GatewayAuthFilter gatewayAuthFilterBean) {
//        FilterRegistrationBean<GatewayAuthFilter> bean = new FilterRegistrationBean<>(gatewayAuthFilterBean);
//        bean.addUrlPatterns("/payouts/ping", "/payouts/createOrder", "/payouts/payinOrderStatus");
//        bean.setOrder(2);
//        return bean;
//    }
//
//    @Bean
//    public FilterRegistrationBean<IpBoundApiKeyFilter> ipBoundApiKeyFilter(IpBoundApiKeyFilter ipBoundApiKeyWebFilter) {
//        FilterRegistrationBean<IpBoundApiKeyFilter> bean = new FilterRegistrationBean<>(ipBoundApiKeyWebFilter);
//        bean.addUrlPatterns("/payouts/ping", "/payouts/createOrder", "/payouts/payinOrderStatus");
//        bean.setOrder(1);
//        return bean;
//    }
```

### Added (disable servlet registration; filters run only in SecurityFilterChain)

```java
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
```

---

## 5. `CorsConfig.java`

Path: `backend/src/main/java/com/pv/couseae/config/CorsConfig.java`

### Removed

```java
config.setAllowedOrigins(List.of("*")); // frontend React app
```

### Added

```java
// "*" is illegal with allowCredentials=true (Postman/browser Origin header).
config.setAllowedOriginPatterns(List.of("*"));
```

---

## 6. `SearchRequest.java`

Path: `backend/src/main/java/com/pv/couseae/utill/SearchRequest.java`

### Removed

```java
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    private int start;
    private int size;
    private String currencyCode = "";

    private String keyword = "";
```

### Added

```java
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequest {
    private int start;
    @JsonAlias("length")
    private int size;
    private String currencyCode = "";

    @JsonAlias("search")
    private String keyword = "";
```

---

## 7. New file: `backend/docker/mongo-uat/seed-all.js`

**Added** (whole file). Seeds UAT db `zenithuat` on Mongo `27018`.

Users/roles upserted (password `Test@1234`):

```javascript
upsert("users", "admin@courseae.com", userDoc(..., "ADMIN", ...));
upsert("users", "testadmin@local.test", userDoc(..., "ADMIN", ...));
upsert("users", "testsubadmin@local.test", userDoc(..., "SUBADMIN", ...));
upsert("users", "testfinance@local.test", userDoc(..., "FINANCE", ...));
upsert("users", "testauditor@local.test", userDoc(..., "AUDITOR", ...));
upsert("users", "testviewer@local.test", userDoc(..., "MERCHANT_VIEWER", ...));
upsert("users", "testreseller@local.test", userDoc(..., "RESELLER", ...));
upsert("users", "testmerchant@local.test", userDoc(..., "MERCHANT", ...));
upsert("users", "testsubmerchant@local.test", userDoc(..., "SUBMERCHANT", ...));
```

Also currencies, countries, acquirers, wallets, fee/limit rules, payment types, dummy payins. Full script is in the file (do not commit live secrets if you copy it elsewhere).

**Removed:** temporary `seed-payin-merchant.js` after the first merchant insert (logic folded into `seed-all.js`).

---

## Files not changed

| File | Note |
|------|------|
| `SecurityConfig.java` | Still `IpBoundApiKeyFilter` then `GatewayAuthFilter`. |
| `JwtAuthenticationFilter.java` | Not on the security chain; servlet registration disabled. |
| `CouseaeApplication.java` | Unchanged |
| `application.yml` / `application-uat.yml` | Still uat, port 8085, Mongo 27018 |

---

## How to run

```powershell
cd C:\Users\admiin\Desktop\courseAE\backend
.\mvnw.cmd spring-boot:run
```

Docker: Mongo `27018`, Redis `6379`. Logs: `logs/couseae_backend.log`.

| Call | Headers |
|------|---------|
| Dashboard (`/user/all`, `/user/merchant/list`) | `Authorization: Bearer <jwt>` |
| Payin `/payins/createOrder` | `merchantAppId`, `merchantSecretId`, `merchantHash` |

Do not send `X-Authenticated-User` with Bearer. Do not use `https://api.courseraeducation.com` until this build is deployed.
