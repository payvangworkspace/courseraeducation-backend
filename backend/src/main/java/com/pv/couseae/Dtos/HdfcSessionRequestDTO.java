package com.pv.couseae.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HdfcSessionRequestDTO {
    private String order_id;
    private String amount;
    private String customer_id;
    private String customer_email;
    private String customer_phone;
    private String payment_page_client_id;
    private String action;
    private String currency;
    private String return_url;
    private String description;
    private String first_name;
    private String last_name;
}