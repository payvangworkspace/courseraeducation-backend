package com.pv.couseae.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Document(collection = "users")
@JsonIgnoreProperties({"password", "username", "authorities"})
public class User implements UserDetails {

    //Personal Details
    @Id
    @NotBlank(message = "Email should not be null")
    @NotNull(message = "Email should not be null")
    @NotEmpty(message = "Email should not be null")
    @Email(message = "Email is not valid")
    private String userId;

    @NotBlank(message = "phone no should not be null")
    @Size(min = 10, max = 16, message = "Phone Number should be contains 10-16 digits")
    private String contactNumber;
    private boolean isContactVerified = false;

    private boolean isEmailVerified = false;

    private double accountBalance = 0.0;
    private BigDecimal holdBalance = BigDecimal.valueOf(0.0);


    @NotBlank(message = "Name should not be empty")
    @Size(max = 20, min = 2, message = "Name should be contain minimum 3 and maximum 20 digits")
    private String fullName;
    private String businessName;
    @Size(max = 4)
    private String shortCode;

    @NotBlank(message = "Password should not be null")
    private String password;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    private String dateOfBirth;

    private String gender;
    private boolean isVerified = false;

    @JsonIgnore
    private String secretKey;
    private String appKey;
    private boolean merchantHostedFlag = false;
    private String processingMode;

    private String role;

    @DBRef
    @JsonIgnore
    private Set<Currency> currencies;

    @DBRef
    @JsonIgnore
    private Set<LocationCountry> countries;

    //Contact Details
//    @DBRef
//    @JsonIgnore
//    private Set<AddressDetails> address;

    private String addressDetails;

    @DBRef
    @JsonIgnore
    private Set<BankDetails> bankDetails;

    private String payinWebhookUrl;

    private String payoutWebhookUrl;

    //account status Details
    private boolean status = true;
    private boolean isAccountNonExpired = true;
    private boolean isCrypto=false;
    private boolean isAccountNonLocked = true;
    private boolean isCredentialsNonExpired = true;
    private boolean isPayoutEnabled = false;
    private boolean isPayoutEnabledViaApp = false;
    private boolean isPayoutBlocked = false;
    private boolean isPayinEnabled = false;
    private boolean isPayoutGstEnabled = false;
    private boolean isPayinGstEnabled = false;
    private boolean isFeeReturnOnRefund = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    @CreatedDate
    private LocalDateTime createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime verificationDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime lastModifiedDate;
    @LastModifiedBy
    private String lastModifiedBy;


    public User(String userId) {
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isEnabled() {
        return status;
    }

}

