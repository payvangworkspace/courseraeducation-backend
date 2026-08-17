package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Document()
public class BusinessDetails {
    @Id
    private String id;

    @NotBlank(message = "Business Name should not be empty")
    @NotNull(message = "Business Name not be empty")
    @NotEmpty(message = "Business Name should not be empty")
    private String businessName;

    @NotBlank(message = "Company Registration No should not be empty")
    @NotNull(message = "Company Registration No not be empty")
    @NotEmpty(message = "Company Registration No should not be empty")
    private String companyRegistrationNo;

    @NotBlank(message = "GST/VAT should not be empty")
    @NotNull(message = "GST/VAT not be empty")
    @NotEmpty(message = "GST/VAT should not be empty")
    private String gstVat;
    private String panSsn;

    @Min(value = 0,message = "Setup/Integration Fees should not be empty")
    private double setupIntegrationFees;

    @Min(value = 0, message = "Settlement Fees should not be empty")
    private double settlementFees;

    @Min(value = 0,message = "Wire Transfer Fees should not be empty")
    private double wireTransferFees;

    @Min(value = 0,message = "Minimum Settlement Amount should not be empty")
    private double minimumSettlementAmount;

    @NotBlank(message = "Business Email should not be empty")
    @NotNull(message = "Business Email not be empty")
    @NotEmpty(message = "Business Email should not be empty")
    @Email(message = "Email is not valid")
    private String businessEmail;
    private String businessAlternateEmail;

    @NotBlank(message = "Business phone number should not be empty")
    @NotNull(message = "Business phone number not be empty")
    @NotEmpty(message = "Business phone number should not be empty")
    @Size(min = 10, max = 16, message = "Mobile Number must contain min 10 and max 16 digits")
    private String phone;
    private String alternatePhone;

    private String websiteUrl;

    // address
    @NotBlank(message = "Address should not be empty")
    @NotNull(message = "Address should not be empty")
    @NotEmpty(message = "Address code should not be empty")
    private String businessAddress;
    @DBRef
    private LocationCity city;
    @Min(value = 100000, message = "Postal code must contains 6 digits")
    @Max(value = 999999,message = "Postal code must contains 6 digits")
    private int postalCode;

    private String businessType;
    private String businessSubType;

    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User user;
}
