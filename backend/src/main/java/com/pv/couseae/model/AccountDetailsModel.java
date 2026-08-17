package com.pv.couseae.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailsModel {
    private String userId;
    private String contactNumber;
    private String fullName;
    private boolean isVerified = false;
    private boolean status = true;
    private String processingMode;
    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = true;
    private boolean isCredentialsNonExpired = true;
    private boolean isPayoutEnabled = false;
    private boolean isPayoutEnabledViaApp = false;
    private boolean isPayinEnabled = false;
    private boolean isPayoutGstEnabled = false;
    private boolean isPayinGstEnabled = false;
    private boolean isFeeReturnOnRefund = false;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime verificationDate;
    private String createdBy;
}
