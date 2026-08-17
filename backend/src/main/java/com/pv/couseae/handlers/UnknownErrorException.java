package com.pv.couseae.handlers;

public class UnknownErrorException extends RuntimeException{
    public UnknownErrorException() {
        super("Something went wrong");
    }
    public UnknownErrorException(String message) {
        super(message);
    }

}
