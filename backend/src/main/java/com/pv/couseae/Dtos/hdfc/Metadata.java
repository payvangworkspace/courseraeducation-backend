package com.pv.couseae.Dtos.hdfc;

import lombok.Data;

@Data
public class Metadata {
    private String order_expiry;
    private String payment_page_client_id;
    private PaymentLinks payment_links;

    private String merchant_payload;
    private String payment_page_sdk_payload;
}
