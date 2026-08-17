package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pv.couseae.utill.DoubleToTwoDecimalSerializer;
import com.pv.couseae.utill.Tracker;
import jakarta.validation.constraints.NotNull;
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
public class UserAccount extends Tracker {
    @Id
    private String userAccountId;

    @NotNull(message = "User should not be empty")
    @JsonIncludeProperties({"userId", "fullName","businessName"})
    @DBRef
    private User user;

    @DBRef
    @JsonIncludeProperties({"currencyId","currencyName","currencyCode"})
    private Currency currency;

    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double amountBalance =0.0;

    @Transient
    private String userFullName;

    public UserAccount(User user) {
        this.user = user;
    }

    public String getUserFullName() {
        return user.getFullName();
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = this.user.getFullName();
    }
}
