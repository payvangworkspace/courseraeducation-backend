package com.pv.couseae.handlers;

public class TransactionLimitException extends RuntimeException {

    public TransactionLimitException(String message) {
        super(message);
    }
}