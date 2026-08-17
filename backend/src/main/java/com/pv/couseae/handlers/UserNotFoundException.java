package com.pv.couseae.handlers;


import com.pv.couseae.utill.ConstantMessage;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException() {
        super(ConstantMessage.USER_NOT_FOUND_EXCEPTION_MESSAGE);
    }
}
