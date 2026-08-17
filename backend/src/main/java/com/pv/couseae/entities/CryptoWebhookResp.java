package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payin_Crypto_webhookResp")
public class CryptoWebhookResp {
    @Id
    private String id;
    private String merchantId;
    private String orderId;
    private String cryptoOrderId;
    private String jsonText;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss a")
    @CreatedDate
    private LocalDateTime createdDate;
}
