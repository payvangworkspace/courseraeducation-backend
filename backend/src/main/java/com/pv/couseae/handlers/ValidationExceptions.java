package com.pv.couseae.handlers;


import com.pv.couseae.utill.ConstantMessage;

public class ValidationExceptions extends RuntimeException{
    public ValidationExceptions() {
        super(ConstantMessage.VALIDATION_EXCEPTION_DEFAULT_MESSAGE);
    }
    public ValidationExceptions(String message) {
        super(message);
    }
}
