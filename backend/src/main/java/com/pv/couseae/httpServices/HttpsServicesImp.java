package com.pv.couseae.httpServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pv.couseae.Dtos.MerchantCreateResponse;
import com.pv.couseae.security.SystemConfigurations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class HttpsServicesImp implements HttpServices {

    private final RestTemplate restTemplate;
//    private final MerchantService merchantService;
    private final SystemConfigurations systemConfig;


    @Override
    public MerchantCreateResponse.DataNode GetCryptoMerchantOnboarding(String merchantname, String merchantId) {

        String apikey=systemConfig.getCryptoAdminApiKey();
        String url = "https://webhook.uatzenithpay.in/api/v1/orders";
        url = systemConfig.getCryptoBaseUrl()+ "/api/v1/merchants";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-admin-api-key", apikey);

        Map<String, String> body = Map.of("name", merchantname,"email", merchantId );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<MerchantCreateResponse> response =restTemplate.postForEntity(url, entity, MerchantCreateResponse.class);

        log.info("HTTP Status: {}", response.getStatusCode());
        log.info("Response Body: {}", response.getBody());

        // ✅ HTTP-level validation
        if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
            log.warn("Merchant creation failed: {}", response.getBody() != null ? response.getBody().getMessage() : "Unknown error");
            return null;
        }

        MerchantCreateResponse responseBody = response.getBody();

        if (responseBody == null || responseBody.getData() == null) {
            log.warn("Merchant creation failed: empty response data");
            return null;
        }

        return responseBody.getData();
    }
    @Override
    public String AdminCryptoSettlementAPI(String merchantId, String networkName, String tokenSymbol, String toAddress, BigDecimal amount, String note) {

        String apikey=systemConfig.getCryptoAdminApiKey();
        String url = "https://webhook.uatzenithpay.in/api/v1/orders";
        url = systemConfig.getCryptoBaseUrl()+ "/api/v1/admin/settlements";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-admin-api-key", apikey);

        networkName="bsc";
        toAddress="0xC22082E4210bD99CD80726Aec0e0fF4024223c3F";
        Map<String, String> body =new LinkedHashMap<>();
        body.put("merchantId",merchantId);
        body.put("networkName",networkName);
        body.put("tokenSymbol",tokenSymbol);
        body.put("toAddress",toAddress);
        body.put("amount",amount.toString());
        body.put("note",note);


        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =restTemplate.postForEntity(url, entity, String.class);

        log.info("HTTP Status: {}", response.getStatusCode());
        log.info("Response Body: {}", response.getBody());

        // ✅ HTTP-level validation
        if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
            log.warn("Merchant creation failed: {}", response.getBody() != null ? response.getBody(): "Unknown error");
            return null;
        }

        String responseBody = response.getBody();

        if (responseBody == null) {
            log.warn("Merchant creation failed: empty response data");
            return null;
        }

        return responseBody;
    }

    public String getRequestWithoutHeader(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    @Async
    @Override
    public void postRequestWithoutHeader(String url, Object requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        log.info("HttpsServicesImp->postRequestWithoutHeader {}",response.getBody());
    }

    @Override
    public String getRequestWithHeader(String url, String token) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/json");

        // Create entity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make GET request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }

    @Override
    public String postRequestWithHeaderAndBearerToken(String url, String token, Object requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authentication", "Bearer " + token);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();
    }
    @Override
    public String postRequestWithCustomHeader(String url, Object generateRequest, HttpHeaders headers) {
        HttpEntity<Object> entity = new HttpEntity<>(generateRequest, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();

    }

        @Override
    public String postRequestWithHeaderAndBasicToken(String url, String userName, String password, Object requestBody) {
        ResponseEntity<String> response;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            String encodedCredentials = Base64.getEncoder().encodeToString((userName+":"+password).getBytes());
            String encodedCredentials = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
            headers.set("Authorization", "Basic " + encodedCredentials);
            //HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<Object> entity = new HttpEntity<>(jsonBody, headers);

            response = restTemplate.postForEntity(url, entity, String.class);
            log.info("HttpsServicesImp->postRequestWithHeaderAndBasicToken {}",response.getBody());
            return response.getBody();
        } catch (RestClientException | JsonProcessingException e) {
            return "";
        }

    }




}
