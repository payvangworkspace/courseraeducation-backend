package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class BankDetails {
    @Id
    private String bankDetailId;

    @NotBlank(message = "Bank Name should not be empty")
    @NotNull(message = "Bank Name not be empty")
    @NotEmpty(message = "Bank Name should not be empty")
    private String bankName;

    @NotBlank(message = "Branch Name should not be empty")
    @NotNull(message = "Branch Name not be empty")
    @NotEmpty(message = "Branch Name should not be empty")
    private String branchName;

    @NotBlank(message = "Account Number should not be empty")
    @NotNull(message = "Account Number not be empty")
    @NotEmpty(message = "Account Number should not be empty")
    private String bankAccountNumber;

    @NotBlank(message = "IFSC Code should not be empty")
    @NotNull(message = "IFSC Code not be empty")
    @NotEmpty(message = "IFSC Code should not be empty")
    private String ifscCode;

    private String cardNumber;

    private String vpa;


    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User user;

}