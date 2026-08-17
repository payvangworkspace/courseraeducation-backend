package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pv.couseae.enums.AddressType;
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
public class AddressDetails {
    @Id
    private String id;
    //Contact Details
    private AddressType addressType;
    @Size(min = 10, max = 16, message = "Mobile Number must contain min 10 and max 16 digits")
    private String mobile;
    private String telephoneNo;
    private String fax;

    private String email;
    @NotBlank(message = "Address should not be empty")
    @NotNull(message = "Address should not be empty")
    @NotEmpty(message = "Address code should not be empty")
    private String address;
    @DBRef
    private LocationCity city;
    @Min(value = 100000, message = "Postal code must contains 6 digits")
    @Max(value = 999999,message = "Postal code must contains 6 digits")
    private int postalCode;

    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User user;
}
