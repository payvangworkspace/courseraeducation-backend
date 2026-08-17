package com.pv.couseae.utill;


import com.pv.couseae.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (authentication.getPrincipal().toString().equalsIgnoreCase("anonymousUser")){
            return "system";
        }
        User user = (User) authentication.getPrincipal();
        return ((User) authentication.getPrincipal()).getUserId();
    }
    public static LocalDateTime createdDate(){
        return LocalDateTime.now();
    }

    public boolean isValidPassowrd(String password) {
        return pattern.matcher(password).matches();
    }
    public String getRedisUserKey(String u) { return "USER:" + u; }
}
