package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MerchantAcquirerTdrMdrMapping {

    @Id
    private String merchantTdrMdrId;

    @DBRef
    @JsonIncludeProperties({"userId","fullName"})
    private User merchant;

    @DBRef
    @JsonIncludeProperties({"userId","fullName"})
    private Acquirer acquirer;

    @DBRef
    @JsonIncludeProperties({"paymentTypeId","paymentTypeName","paymentTypeCode"})
    private PaymentType paymentType;

    @DBRef
    @JsonIncludeProperties({"mopTypeId","mopTypeName","mopTypeCode"})
    private MOPType mopType;

    private int priority;

    private double amountLimit;

    private double gstVat;

    @DBRef
    private List<MerchantCharges> merchantCharges;


}
