package com.pv.couseae.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)   // ignore User's extra fields (accountBalance, password, etc.)
public class UserListModel {
    private String userId;
    private String fullName;
    private String contactNumber;
    private String businessName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime verificationDate;
}