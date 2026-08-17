package com.pv.couseae.handlers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");

        Map<String, Object> body = new HashMap<>();
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            body.put("source", "main-service");
            body.put("error", "Bad Request");
            body.put("message", "Invalid request payload or headers");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body.toString());
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            body.put("source", "main-service");
            body.put("error", "Forbidden");
            body.put("message", "You don’t have permission to access this resource");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body.toString());
        } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            body.put("source", "main-service");
            body.put("error", "Unauthorized");
            body.put("message", "Authentication required or token is invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body.toString());
        }
        body.put("source", "main-service");
        body.put("error", "Internal Server Error");
        body.put("message", "Something went wrong");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body.toString());

    }
}
