package com.pv.couseae.Dtos.hdfc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    private String expiry_year;
    private String card_reference;
    private Boolean saved_to_locker;
    private String expiry_month;
    private String name_on_card;
    private String card_issuer;
    private String last_four_digits;
    private Boolean using_saved_card;
    private String card_fingerprint;
    private String card_isin;
    private String card_type;
    private String card_brand;
    private Boolean using_token;
    private List<Object> tokens;
    private String token_type;
    private String card_issuer_country;
    private String juspay_bank_code;
    private String extended_card_type;
    private String payment_account_reference;
    private String card_sub_type_category;
}