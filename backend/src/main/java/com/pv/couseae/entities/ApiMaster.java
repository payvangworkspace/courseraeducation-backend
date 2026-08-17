package com.pv.couseae.entities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "api_master")
public class ApiMaster {
    @Id
    private String id;

    private String aggregatorCode;        // e.g., HDFC, PAYTM, RAZORPAY
    private String apiName;               // e.g., "CREATE_SESSION", "CHECK_STATUS"
    private String baseUrl;               // e.g., https://smartgateway.hdfcuat.bank.in
    private String endpoint;              // e.g., /session
    private String httpMethod;            // e.g., POST, GET
    private String type;           // e.g., Payin,Payout
    private String merchantId;
    private String secretKey;
    private String clientId;
    private String responseUrl;
    private String webhoockUrl;
    private String responsekey;

    private boolean active;               // enabled/disabled

    // Default headers (can be overridden)
    private Map<String, String> headers;

    // Sample request body structure (JSON template)
    private Map<String, Object> requestTemplate;

    // Environment info
    private String environment;           // e.g., UAT, PROD

    // For logging and versioning
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
