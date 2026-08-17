package com.pv.couseae.Dtos.hdfc;

import lombok.Data;

@Data
public class PaymentGatewayResponse {
    private String resp_code;
    private String rrn;
    private String created;
    private String epg_txn_id;
    private String resp_message;
    private String auth_id_code;

    private String txn_id;
    private String network_error_message;
    private String network_error_code;
    private String arn;
    private String gateway_merchant_id;
    private String eci;
    private String auth_ref_num;
    private String umrn;
    private String current_blocked_amount;
    private String payer_ifsc;
    private String xid;
    private String cvv_check;
}