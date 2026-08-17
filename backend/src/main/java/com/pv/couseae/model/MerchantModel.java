package com.pv.couseae.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MerchantModel {
    private String userId;
    private String contactNumber;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String addressDetails;
    private String shortCode;
    private boolean isVerified = false;

    private String businessName;
    private String gstVat;
    private String panSsn;
    private String websiteUrl;

    private String appKey;
    private String secretKey;
    private String processingMode;
    private String businessType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime verificationDate;
    private String createdBy;

    private String businessSubType;
    private boolean loginLogo;
    private boolean brandLogo;
    private boolean pageLogo;


}
