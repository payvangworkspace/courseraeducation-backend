package com.pv.couseae.handlers;

import lombok.Getter;


@Getter
public class IntegrationErrorException extends RuntimeException {
    private final String statusCode;

    public IntegrationErrorException() {
        super("Invalid data");
        this.statusCode = null;
    }


    public IntegrationErrorException(String message) {
        super(message);
        this.statusCode = "400";
    }

    public IntegrationErrorException(String message, String statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}
