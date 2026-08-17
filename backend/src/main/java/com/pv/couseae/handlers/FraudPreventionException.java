package com.pv.couseae.handlers;

public class FraudPreventionException extends RuntimeException{
    public FraudPreventionException(String message) {
        super(message);
    }
    public FraudPreventionException() {
        super("Blocked by FraudPrevention");
    }

}
