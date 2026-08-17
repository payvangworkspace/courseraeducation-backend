package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ResellerMapping {
    @Id
    private String resellerMerchantId;

    @DBRef
    @JsonIncludeProperties({"userId","fullName"})
    private User merchantId;

    private String merchantFullName;
    private String merchantUserName;

    @DBRef
    @JsonIncludeProperties({"userId","fullName"})
    private User resellerId;

    private boolean isFixCharge;
    private double vendorCharge;

    public ResellerMapping(String id) {
        this.resellerMerchantId = id;
    }
}
