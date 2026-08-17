package com.pv.couseae.handlers;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException() {
        super("You haven't permission for this");
    }

}
