package com.pv.couseae.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface JwtService {
    String extractUserName(String token);

    String generateToken(UserDetails userDetails);
    long getExpiryTime(String token);

    List<String> extractRoles(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
