package com.pv.couseae.handlers;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String SOURCE = "Payout-service";

    // ✅ New — catches @Valid/@NotBlank etc. failures on request DTOs (e.g.
    // PayoutTransferReq.lastName) and surfaces the SPECIFIC field message
    // ("Sender last name is required") instead of Spring's default verbose
    // FieldError dump. This must be handled explicitly — MethodArgumentNotValidException
    // is not a RuntimeException, so the runTimeException()/globalException()
    // catch-alls below would never have caught it anyway; it was previously
    // falling through to Spring's own DefaultHandlerExceptionResolver, which
    // returns its own response body shape rather than your Payout-service format.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = (fieldError != null && fieldError.getDefaultMessage() != null)
                ? fieldError.getDefaultMessage()
                : "Validation failed";

        log.warn("Validation failed — {}", message);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("source", SOURCE);
        body.put("status", "fail");
        body.put("message", message);
        return body;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleExpiredJwtException(ExpiredJwtException ex) {
        String message = safeMessage(ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("source", SOURCE);
        error.put("error", message);
        return error;
    }

    @ExceptionHandler(TransactionLimitException.class)
    public Map<String, String> handleTxnLimit(TransactionLimitException ex) {
        String message = safeMessage(ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("source", SOURCE);
        error.put("error", message);
        return error;
    }

    // Catches the specific InsufficientBalanceException thrown from
    // WalletService / PayoutService, before it can escape to ErrorPageFilter.
    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public Map<String, String> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Payout rejected — {}", ex.getMessage());
        String message = safeMessage(ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("source", SOURCE);
        error.put("error", message);
        return error;
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleJwtException(JwtException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("source", SOURCE);
        error.put("error", ex.getMessage());
        return error;
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("source", SOURCE);
        error.put("error", ex.getMessage());
        return error;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> noHandlerFoundException(NoHandlerFoundException ex) {
        log.error("NoHandlerFoundException", ex);
        String message = safeMessage(ex.getMessage());
        Map<String, String> body = new LinkedHashMap<>();
        body.put("source", SOURCE);
        body.put("status", "fail");
        body.put("message","Something went wrong");
        body.put("errors", message);
        return body;
    }

    // Re-enabled: this was previously commented out, which is the actual
    // reason unhandled RuntimeExceptions (like the plain "Insufficient balance"
    // one from WalletService) fell through to ErrorPageFilter and surfaced as
    // a misleading "Unauthorized" response instead of their real message.
    //
    // This MUST stay active — Spring resolves @ExceptionHandler methods by
    // most-specific exception type match regardless of declaration order, so
    // InsufficientBalanceException/TransactionLimitException/etc. above will
    // still be matched first when applicable; this is only the fallback.
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> runTimeException(RuntimeException ex) {
        String message = safeMessage(ex.getMessage());
        log.error("RuntimeException: {}", message, ex);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("source", SOURCE);
        body.put("status", "fail");
        body.put("message", message);
        body.put("errors", message);

        return body;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> globalException(Exception ex) {
        String message = safeMessage(ex.getMessage());
        log.error("GlobalException: {}", message, ex);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("source", SOURCE);
        body.put("status", "fail");
        body.put("message", "Something went wrong");
        body.put("errors", message);

        return body;
    }

    private String safeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.replaceAll("[\\r\\n]", " ").trim();
    }
}