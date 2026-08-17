package com.pv.couseae.Dtos.hdfc;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TxnDetail {
    private String txn_id;
    private String order_id;
    private String status;
    private String error_code;
    private double net_amount;
    private Double surcharge_amount;
    private Double tax_amount;
    private double txn_amount;
    private Double offer_deduction_amount;

    private int gateway_id;
    private String currency;

    private Map<String, Object> metadata;

    private boolean express_checkout;
    private boolean redirect;
    private String txn_uuid;
    private String gateway;
    private String error_message;

    private String created;
    private String last_updated;
    private String txn_flow_type;

    private List<TxnAmountBreakup> txn_amount_breakup;
}
