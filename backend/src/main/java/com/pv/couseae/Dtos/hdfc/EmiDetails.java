package com.pv.couseae.Dtos.hdfc;

import lombok.Data;

import java.util.List;

@Data
public class EmiDetails {
    private String bank;
    private String monthly_payment;
    private String interest;
    private String subvention_amount;
    private String conversion_details;
    private String principal_amount;
    private String additional_processing_fee_info;
    private String tenure;

    private List<Object> subvention_info;

    private String emi_type;
    private String processed_by;
}
