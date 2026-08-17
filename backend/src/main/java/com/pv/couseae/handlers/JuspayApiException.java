package com.pv.couseae.handlers;

import lombok.Getter;

@Getter
public class JuspayApiException extends RuntimeException {

    private final String responseCode;
    private final String responseMessage;

    public JuspayApiException(String responseCode, String responseMessage) {
        super(responseMessage);
        this.responseCode    = responseCode;
        this.responseMessage = responseMessage;
    }
}