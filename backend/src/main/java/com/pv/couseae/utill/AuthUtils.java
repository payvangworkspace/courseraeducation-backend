package com.pv.couseae.utill;


import com.pv.couseae.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Component
public class AuthUtils {
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +           // at least one digit
                    "(?=.*[a-z])" +            // at least one lowercase
                    "(?=.*[A-Z])" +            // at least one uppercase
                    "(?=.*[@#$%^&+=!])" +      // at least one special char
                    "(?=\\S+$).{8,}$";         // no whitespace + min length 8
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    public static String authUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equalsIgnoreCase(principal.toString())) {
            return "system";
        }
        // GatewayAuthFilter JWT path stores the email as a String.
        // JwtAuthenticationFilter may store UserDetails; older paths store User.
        if (principal instanceof User user) {
            return user.getUserId();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email) {
            return email;
        }
        return principal.toString();
    }
    public static LocalDateTime createdDate(){
        return LocalDateTime.now();
    }

    public boolean isValidPassowrd(String password) {
        return pattern.matcher(password).matches();
    }
    public String getRedisUserKey(String u) { return "USER:" + u; }
}
