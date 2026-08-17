package com.pv.couseae.auth;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String email;
    private String contactNumber;
    private String fullName;
    private String token;
    private String userRole;
    private boolean isVerified;
    private boolean isPayoutEnabledViaApp;
}
