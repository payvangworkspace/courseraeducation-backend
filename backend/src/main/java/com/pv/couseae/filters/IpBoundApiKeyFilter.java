package com.pv.couseae.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.entities.IpApiKeyInfo;
import com.pv.couseae.entities.User;
import com.pv.couseae.repos.IpApiKeyRepo;
import com.pv.couseae.repos.UserRepoDB;
import com.pv.couseae.services.IPEncryptionService;
import com.pv.couseae.utill.IpBoundKeyGenerator;
import com.pv.couseae.utill.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
//@Order(1)
public class IpBoundApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.service-name}")
    private String serviceName;

    /**
     * Headers this filter GRANTS on the API-key path. Downstream controllers treat
     * X-Validated-Merchant-Id as proof of identity, so a client-supplied copy must never
     * survive into the application — see stripGrantedHeaders().
     */
    private static final Set<String> GRANTED_HEADERS = Set.of(
            "x-client-ip",
            "x-validated-merchant-id",
            "x-authenticated-user");

    private final IpApiKeyRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final IpBoundKeyGenerator keyGen; // same secret
    private final UserRepoDB userRepo;
    private final IPEncryptionService ipEncryptionService;
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper; // reused, injected — not created per-request
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // NOTE: no @Qualifier here — this payout-service context only defines a
    // single ObjectMapper bean (redisObjectMapper in RedisConfig), so an
    // unqualified injection resolves to it automatically. This is safe for
    // this filter specifically because it only ever calls
    // objectMapper.readTree(...) (parses into a generic JsonNode tree), never
    // convertValue(...)/readValue(...) into a concrete POJO — so Jackson's
    // default-typing config on that mapper never triggers subtype resolution
    // here. If this filter is ever changed to deserialize into a concrete
    // class, add a dedicated plain (non-default-typing) ObjectMapper bean to
    // this service and qualify against that instead.
    public IpBoundApiKeyFilter(IpApiKeyRepo repo,
                               PasswordEncoder encoder,
                               IpBoundKeyGenerator keyGen,
                               UserRepoDB userRepo,
                               IPEncryptionService ipEncryptionService,
                               SecurityProperties securityProperties,
                               ObjectMapper objectMapper) {
        this.repo = repo;
        this.passwordEncoder = encoder;
        this.keyGen = keyGen;
        this.userRepo = userRepo;
        this.ipEncryptionService = ipEncryptionService;
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight carries no credentials of any kind. Rejecting it here produces a
        // response with no Access-Control-* headers, which the browser reports as a CORS
        // error rather than a 401 — a confusing failure mode with an easy fix.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String contextPath = request.getContextPath();     // e.g. /payout_service
        String uri = request.getRequestURI();              // e.g. /payout_service/admin/keys/TestIPKey

        String path = uri.substring(contextPath.length()); // e.g. /admin/keys/TestIPKey
        log.info("In IpBoundApiKeyFilter ContextPath={}, URI={}, FinalPath={}",
                request.getContextPath(), request.getRequestURI(), path);

        return uri.equals("/favicon.ico")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || securityProperties.getPublicPaths().stream()
                .anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            log.info("Inside do internal filter");
            String path = req.getRequestURI();

            log.info("IpBoundApiKeyFilter path: {}", path);

            // ── JWT BYPASS ───────────────────────────────────────────────────
            // A Bearer token means this is a dashboard/console call rather than a
            // merchant server-to-server API call, so the IP-bound key requirement does
            // not apply. Nothing here trusts the token: GatewayAuthFilter runs straight
            // after this filter and performs the actual validation. All this decides is
            // "not an API-key request".
            //
            // The request is stripped of granted headers before being passed on — see
            // stripGrantedHeaders() for why that is essential on this path.
            if (hasBearerToken(req)) {
                log.info("Bearer token present — skipping ZIPAPIKEY check for path {}", path);
                chain.doFilter(stripGrantedHeaders(req), res);
                return;
            }

            // ── MERCHANT APP/SECRET BYPASS ───────────────────────────────────
            // Payin endpoints (createOrder, createCryptoOrder, payinOrderStatus)
            // authenticate with merchantAppId + merchantSecretId + merchantHash.
            // That is not a ZIPAPIKEY call. Validate keys here, then grant the
            // same identity headers GatewayAuthFilter expects.
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

            if (path.startsWith("/admin/keys/createIPKey")) {
                CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(req);
                // Read body manually BEFORE calling chain.doFilter
                String body = wrappedRequest.getBodyAsString();
                if (!body.isBlank()) {
                    JsonNode node = objectMapper.readTree(body);
                    String merchantId = node.path("merchantId").asText(null);
                    if (merchantId == null) {
                        unauthorized(res, "Missing merchantId");
                        return;
                    }
                    // compare with DB value — null-safe
                    Optional<User> users = userRepo.findByUserId(merchantId);
                    if (users.isEmpty() || !merchantId.equals(users.get().getUserId())) {
                        unauthorized(res, "Invalid merchantId");
                        return;
                    } else {
                        User user = users.get();
                        log.info("The user id {} is matched in DB..", user.getUserId());
                        String clientIp = extractClientIp(req);
                        log.info("Inside Create api key Client IP: " + clientIp);
                    }
                }
                chain.doFilter(wrappedRequest, res);
                return;
            }

            String headerAPiKey = getHeaderIgnoreCase(req, "ZIPAPIKEY");
            // removed: logging of raw API key value (sensitive data exposure)
            if (headerAPiKey == null || headerAPiKey.isBlank()) {
                unauthorized(res, "Missing API key...");
                return;
            }

            // Wrap early so the body can be read here AND still be readable
            // by downstream filters/controllers after this filter finishes.
            CachedBodyHttpServletRequest cachedReq = new CachedBodyHttpServletRequest(req);

            // expected format: keyId.nonce.token
            String[] parts = headerAPiKey.split("\\.");
            if (parts.length < 3) {
                log.info("Bad Api key .....");
                unauthorized(res, "Bad API key format");
                return;
            }
            String nonce = parts[0];
            String token = parts[1];
            String encMerchant = parts[2];

            String clientIp = extractClientIp(req);
            log.info("Client IP: " + clientIp);

            String decryptedMerchant = ipEncryptionService.decrypt(encMerchant);
            log.info("decryptedMerchant merchant --->" + decryptedMerchant);
            IpApiKeyInfo entity = repo.findByAllowedIpsAndMerchantId(clientIp, decryptedMerchant)
                    .filter(e -> e.getActive() && e.getAllowedIps() != null)
                    .orElse(null);
            log.info("The entity is :" + entity);
            if (entity == null) {
                unauthorized(res, "Invalid key id...");
                return;
            }

            // NOTE: exact-string IP match only — does not support CIDR ranges.
            // If allowedIps is ever expected to represent a subnet, replace with
            // a proper CIDR-aware match (e.g. ipMatches(clientIp, entity.getAllowedIps())).
            log.info("Client IP:{} and entity.getAllowedIps():{}", clientIp, entity.getAllowedIps());
            if (!clientIp.equals(entity.getAllowedIps())) {
                unauthorized(res, "IP not allowed...");
                return;
            }

            // removed: logging of decrypted merchant identity (sensitive data exposure)
            if (!decryptedMerchant.equalsIgnoreCase(entity.getMerchantId())) {
                unauthorized(res, "Invalid Merchant...");
                return;
            }

            String candidate = nonce + "." + token;
            if (!passwordEncoder.matches(candidate, entity.getKeyHash())) {
                unauthorized(res, "Invalid token...");
                return;
            }

            // Cross-check: the merchantId inside the request body (if present)
            // must match the merchant authenticated via the API key/token.
            // Prevents a valid key for Merchant A being used to submit a
            // payload claiming to act as Merchant B.
            String body = cachedReq.getBodyAsString();
            if (!body.isBlank()) {
                JsonNode node = objectMapper.readTree(body);
                if (node.has("merchantId") || node.has("merchantId")) {
                    String bodyMerchantId = node.has("merchantId")
                            ? node.path("merchantId").asText(null)
                            : node.path("merchantId").asText(null);
                    if (bodyMerchantId != null && !bodyMerchantId.equalsIgnoreCase(entity.getMerchantId())) {
                        unauthorized(res, "Merchant mismatch between API key and request body");
                        return;
                    }
                }
            }

            chain.doFilter(wrapGrantedHeaders(cachedReq, clientIp, entity.getMerchantId()), res);

        } catch (RuntimeException ex) {
            log.error("RuntimeException in IpBoundApiKeyFilter: ", ex);

            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            // Internal exception text is not returned to the caller — it can leak stack
            // detail, entity ids and config. The log above has the full picture.
            res.getWriter().write(
                    "{\"source\":\"" + serviceName + "\",\"error\":\"Runtime Exception\","
                            + "\"message\":\"Internal error processing request\"}");
        }
    }

    /**
     * True when a syntactically present Bearer token is on the request.
     *
     * Deliberately does NOT parse or validate the token — that is GatewayAuthFilter's job,
     * and it runs immediately after this filter. All this answers is "is this a JWT-style
     * request rather than an API-key one". A garbage Bearer value gets past this check and
     * is then rejected downstream, which is the correct division of responsibility.
     */
    private boolean hasBearerToken(HttpServletRequest req) {
        String auth = getHeaderIgnoreCase(req, "Authorization");
        if (auth == null || auth.isBlank()) {
            return false;
        }
        return auth.regionMatches(true, 0, "Bearer ", 0, 7)
                && !auth.substring(7).trim().isEmpty();
    }

    /**
     * Injects identity headers after ZIPAPIKEY or merchantAppId/secret validation.
     * GatewayAuthFilter reads X-Authenticated-User from this wrapper.
     */
    private HttpServletRequest wrapGrantedHeaders(HttpServletRequest req, String clientIp, String merchantId) {
        return new HttpServletRequestWrapper(req) {
            private final Map<String, String> customHeaders = new LinkedHashMap<>();

            {
                customHeaders.put("X-Client-IP", clientIp);
                customHeaders.put("X-Validated-Merchant-Id", merchantId);
                customHeaders.put("X-Authenticated-User", merchantId);
            }

            private String lookup(String name) {
                if (name == null) return null;
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

    /**
     * Blanks any client-supplied copy of the headers this filter grants.
     *
     * WHY THIS IS REQUIRED ON THE JWT PATH:
     * On the API-key path the wrapper above SETS X-Validated-Merchant-Id, so whatever the
     * client sent is overwritten. The JWT path sets nothing — so without this, a caller
     * could send:
     *
     *     Authorization: Bearer &lt;their own perfectly valid token&gt;
     *     X-Validated-Merchant-Id: some-other-merchant
     *
     * and every downstream handler that reads that header would act as the other merchant.
     * On several endpoints that header IS the entire authorisation check.
     *
     * Nginx should also be blanking these (proxy_set_header X-Validated-Merchant-Id "";),
     * but a header that functions as authorisation warrants defence in depth.
     */
    private HttpServletRequest stripGrantedHeaders(HttpServletRequest req) {
        return new HttpServletRequestWrapper(req) {

            private boolean blocked(String name) {
                return name != null && GRANTED_HEADERS.contains(name.toLowerCase());
            }

            @Override
            public String getHeader(String name) {
                return blocked(name) ? null : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return blocked(name) ? Collections.emptyEnumeration() : super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = new ArrayList<>();
                Enumeration<String> original = super.getHeaderNames();
                while (original.hasMoreElements()) {
                    String n = original.nextElement();
                    if (!blocked(n)) {
                        names.add(n);
                    }
                }
                return Collections.enumeration(names);
            }
        };
    }

    private void unauthorized(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String body = "{ \"error\": \"" + reason + "\", \"source\": \"" + serviceName + "\" }";
        response.getWriter().write(body);
    }

    // NOTE: This trusts client-supplied forwarding headers (X-Forwarded-For, etc.)
    // before falling back to REMOTE_ADDR. This is only safe if your reverse proxy
    // (e.g. Nginx) explicitly OVERWRITES these headers with the real connecting IP
    // rather than passing through whatever the client sent. Confirm:
    //   proxy_set_header X-Forwarded-For $remote_addr;
    // is set — not $proxy_add_x_forwarded_for, which appends to client-supplied values.
    //
    // This matters more than usual here: the IP is one half of the API key lookup
    // (findByAllowedIpsAndMerchantId), so a spoofable value weakens the binding the
    // whole filter exists to enforce.
    private String extractClientIp(HttpServletRequest request) {
        String[] headersToCheck = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headersToCheck) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    // --- Custom wrapper to cache request body ---
    public static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            return new ServletInputStream() {
                private final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);

                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    // Not needed for synchronous reads
                }
            };
        }

        /** Also override getReader() — Spring's form/JSON binding may use it instead. */
        @Override
        public java.io.BufferedReader getReader() {
            return new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
        }

        public String getBodyAsString() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }

    private String getHeaderIgnoreCase(HttpServletRequest req, String name) {
        Enumeration<String> headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(name)) {
                return req.getHeader(headerName);
            }
        }
        return null;
    }
}