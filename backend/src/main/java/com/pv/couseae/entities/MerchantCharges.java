package com.pv.couseae.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pv.couseae.utill.DoubleToTwoDecimalSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MerchantCharges {
    @Id
    private String merchantChargeId;
    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double gstVat;
    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double merchantCharge;

    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double pgCharge;

    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double bankCharge;

    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double minimumAmountLimit;

    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double maximumAmountLimit;
    private boolean isFixCharge;

    public MerchantCharges(String merchantChargeId) {
        this.merchantChargeId = merchantChargeId;
    }

    public double getPgCharge() {
        pgCharge = merchantCharge - bankCharge;
        return pgCharge;
    }

    public void setPgCharge(double pgCharge) {
        this.pgCharge = this.merchantCharge - this.bankCharge;
    }
}
