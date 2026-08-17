package com.pv.couseae.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PayinReqCrypto {
    private String appid;
    private String merchantId;
    private BigDecimal payableAmount;
    private BigDecimal fiatAmount;
    private String txnType;
    private String chainType;
    private String coinType;
    private String fiatCurrCode;
    private String walletAddress;
    private String orderId;
    private String firstname;
    private String lastname;
    private String emailId;
    private String mobileNo;
    private String return_url;
    private String cancel_url;
    private String callback_url;
    private String paymentMode;
    private String paymentRemarks;
    private String clientIP;
    private String udf1;
    private String udf2;
    private String udf3;
    private String udf4;
    private String udf5;

}
