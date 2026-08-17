package com.pv.couseae.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayinResponseCrypto {
    private String orderId;
    private String amount;
    private String ordRequestId;
    private String cryptoOrderId;
    private String chainType;
    private String coinType;
    private String fiatCurrCode;
    private String walletAddress;
    private String txnType;
    private String txnMethod;
    private String firstname;
    private String lastname;
    private String emailId;
    private String mobileNo;
    private String return_url;
    private String paymentlink;
    private String linkexpirytime;
    private String qrCode;
    private String qrIntentURL;
    private String message;
    private String statusCode;
    private String txnResponseKey;
}
