package com.pv.couseae.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pv.couseae.entities.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistrationModel {

    @NotBlank(message = "Email should not be null")
    @NotNull(message = "Email should not be null")
    @NotEmpty(message = "Email should not be null")
    @Email(message = "Email is not valid")
    private String userId;

    @NotBlank(message = "phone no should not be null")
    @Size(min = 10, max = 16, message = "Phone Number should be contains 10-16 digits")
    private String contactNumber;

    @NotBlank(message = "Name should not be empty")
    @Size(max = 20, min = 2, message = "Name should be contain minimum 3 and maximum 20 digits")
    private String fullName;

    @NotBlank(message = "Password should not be null")
    private String password;


    private String roleId;

    private String acquirerCode;
    private String acquirerPgId;
    private String acquirerPgKey;
    private String acquirerPgPassword;

    private String merchantId;
    private String secretKey;
    private User acquirer;

    //MerchantModel
    private String businessName;
    @JsonAlias("panSSN")
    private String panSsn;
    @JsonAlias("gstVAT")
    private String gstVat;
    private String website;

    // usersPermissions
    private Map<String, String> permissions;

    private String businessType;
    @JsonAlias("subIndustry")
    private String businessSubType;
}
