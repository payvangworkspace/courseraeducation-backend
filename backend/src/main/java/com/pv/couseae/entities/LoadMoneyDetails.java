package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pv.couseae.enums.TransferType;
import com.pv.couseae.utill.DoubleToTwoDecimalSerializer;
import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class LoadMoneyDetails extends Tracker {
    @Id
    private String loadMoneyDetailsId;

    @DBRef
    @JsonIncludeProperties({"userAccountId","user"})
    private UserAccount userAccount;
    @DBRef
    @JsonIncludeProperties({"currencyId","currencyName","currencyCode"})
    private Currency currency;


    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double previousBalance;
    private double amount;
    private double updatedBalance;

    private TransferType transactionTypes;

    private String remark;
    private String receiptId;

    private String imageName;
    @JsonIgnore
    private String imageType;
    @JsonIgnore
    private byte[] image;

    @Transient
    private String userFullName;

    @Transient
    private String userId;

    public String getUserFullName() {
        return userAccount.getUser().getFullName();
    }


    public String getUserId() {
        return userAccount.getUser().getUserId();
    }

}
