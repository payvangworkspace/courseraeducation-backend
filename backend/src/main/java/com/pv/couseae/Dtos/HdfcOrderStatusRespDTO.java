package com.pv.couseae.Dtos;

import com.pv.couseae.Dtos.hdfc.*;
import lombok.Data;

import java.util.List;

@Data
public class HdfcOrderStatusRespDTO {

    private String customer_email;
    private String customer_phone;
    private String customer_id;
    private int status_id;
    private String status;
    private String id;
    private String merchant_id;
    private double amount;
    private String currency;
    private String order_id;
    private String date_created;
    private String last_updated;
    private String return_url;
    private String product_id;

    private HdfcSessionResponseDTO.PaymentLinks payment_links;

    private String udf1;
    private String udf2;
    private String udf3;
    private String udf4;
    private String udf5;
    private String udf6;
    private String udf7;
    private String udf8;
    private String udf9;
    private String udf10;

    private String txn_id;
    private String payment_method_type;
    private String auth_type;
    private String payment_method;
    private boolean refunded;
    private double amount_refunded;
    private double effective_amount;
    private String resp_code;
    private String resp_message;
    private String bank_error_code;
    private String bank_error_message;
    private String txn_uuid;
    // ADD THIS
    private Card card;
    private TxnDetail txn_detail;
    private PaymentGatewayResponse payment_gateway_response;

    private int gateway_id;
    private EmiDetails emi_details;

    private String payer_vpa;
    private UpiDetails upi;

    private Metadata metadata;

    private String gateway_reference_id;
    private List<Object> offers;
    private double maximum_eligible_refund_amount;
    private String order_expiry;
    private String resp_category;
}
