package com.pv.couseae.Dtos.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAcquirerDTO {

    private String merchantTdrMdrId;

    private String merchantUserId;
    private String merchantFullName;

    private String acquirerUserId;
    private String acquirerFullName;
    private String acquirerCode;

    private boolean isPayin;
    private String acquirerPgId;
    private String acquirerPgKey;
    private String acquirerPgPassword;


    private boolean isPayout;
    private String acquirerPayoutPgId;
    private String acquirerPayoutPgKey;
    private String acquirerPayoutPgPassword;

    private boolean aquirerstatus;

    private String payinWebhookUrl;
    private String payoutWebhookUrl;

    private String paymentTypeId;
    private String paymentTypeName;
    private String paymentTypeCode;

    private String mopTypeId;
    private String mopTypeName;
    private String mopTypeCode;

    private int priority;
    private double amountLimit;
    private double mapping_gstVat;
    private List<MerchantDto> merchantList;

}

