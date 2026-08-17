package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pv.couseae.utill.Tracker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import static com.pv.couseae.utill.RegExAndValidations.IPV4_REGEX;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PayoutIpWhitelist extends Tracker {

    @Id
    private String payoutIpWhitelistId;
    @DBRef
    @JsonIgnoreProperties({"contactNumber","dateOfBirth","gender","accountNonLocked","accountNonExpired","credentialsNonExpired"})
    private User user;

    @NotNull(message = "IP address should not be null")
    @NotEmpty(message = "IP address should not be null")
    @NotBlank(message = "IP address should not be null")
    @Pattern(regexp = IPV4_REGEX, message = "IP address is not valid")
    private String ipAddress;

    private String systemName;
    private String ipAddressDesc;
}
