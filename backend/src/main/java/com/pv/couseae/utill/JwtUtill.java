package com.pv.couseae.utill;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtill {
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    @Value("${token}")
    private String jwtSigningKey;

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public long getExpiryTime(String token) {
        return extractExpiration(token).getTime(); // in milliseconds
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Add roles (Spring Security authorities) as claim
//       List<String> lst=userDetails.getAuthorities().stream()
//                .map(grantedAuthority -> grantedAuthority.getAuthority()).toList();
//       log.info("In Token Creation Roles: "+lst);
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()).toList());

        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                .getBody();
    }
    public Authentication getAuthentication(String token) {
        // Parse the token
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey)   // your secret key or public key
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Extract username (subject) from token
        String username = claims.getSubject();

        // Extract roles/authorities if stored in token
        List<String> roles = claims.get("roles", List.class);

        // Convert roles to GrantedAuthority
        List<GrantedAuthority> authorities = roles == null ?
                Collections.emptyList() :
                roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // Build authenticated object
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
    public String validateAndGetSubject(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSigningKey)
                    .build()
                    .parseClaimsJws(token);
            return claims.getBody().getSubject();

        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("JWT expired at "
                    + e.getClaims().getExpiration() + ", current time: " + new Date(), e);
        } catch (UnsupportedJwtException e) {
            throw new BadCredentialsException("Unsupported JWT: " + e.getMessage(), e);
        } catch (MalformedJwtException e) {
            throw new BadCredentialsException("Malformed JWT: " + e.getMessage(), e);
        } catch (SignatureException e) {
            throw new BadCredentialsException("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("JWT claims string is empty", e);
        }
    }
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
