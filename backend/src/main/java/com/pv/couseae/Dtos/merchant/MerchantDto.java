package com.pv.couseae.Dtos.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDto {
    private double merchant_gstVat;
    private double merchantCharge;
    private double pgCharge;
    private double bankCharge;
    private double minimumAmountLimit;
    private double maximumAmountLimit;
    private boolean isFixCharge;
}
