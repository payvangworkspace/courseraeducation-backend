package com.pv.couseae.httpServices;

import com.pv.couseae.Dtos.MerchantCreateResponse;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;

public interface HttpServices {

    MerchantCreateResponse.DataNode GetCryptoMerchantOnboarding(String merchantname, String merchantId);

    String AdminCryptoSettlementAPI(String merchantId, String networkName, String tokenSymbol, String toAddress, BigDecimal amount, String note);

    String getRequestWithoutHeader(String url);
    String getRequestWithHeader(String url,String token );

    void postRequestWithoutHeader(String url, Object requestBody);
    String postRequestWithHeaderAndBearerToken(String url, String token, Object requestBody);
    String postRequestWithHeaderAndBasicToken(String url, String userName, String Password, Object requestBody);

//    @Async
//    void sendResponseToClientReturnUrl(String url, Orders generatedOrder);
//
//    void sendPayinWebhook(String url, Orders generatedOrder);
//    void sendPayoutWebhook(String url, TransactionPayout transactionPayout);
//
//    void sendWebhookOnReturnUrl(String returnUrl, OrderStatusResponseModel response, String message, String status, String statusCode);

    String postRequestWithCustomHeader(String url, Object generateRequest, HttpHeaders headers);
}

