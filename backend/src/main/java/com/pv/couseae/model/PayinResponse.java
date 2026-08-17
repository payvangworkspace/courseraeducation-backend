package com.pv.couseae.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayinResponse {

    private String orderId;
    private String amount;
    private String ordRequestId;
    private String ordTransactionId;
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
    private Map<String, String> upiIntentApps;

    private String vanNumber;
    private String vanIfsc;
    private String vanBankName;
    private String vanAccountName;

    private String message;
    private String statusCode;
    private String txnResponseKey;

    // Card Details
    private String cardBrand;
    private String cardType;
    private String cardIssuer;
    private String cardLastFourDigits;
    private String cardHolderName;
    private String cardIsin;

    // Gateway Details
    private String rrn;
}
