package com.pv.couseae.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AcquirerModel {

    private String acquirerId;
    @NotBlank(message = "Name should not be empty")
    @Size(max = 20, min = 2, message = "Name should be contain minimum 3 and maximum 20 digits")
    private String fullName;

    @NotBlank(message = "AcquirerModel Code should not be null")
    @NotNull(message = "AcquirerModel Code should not be null")
    @NotEmpty(message = "AcquirerModel Code should not be null")
    private String acquirerCode;

    private boolean isPayin;
    private boolean isPayout;

    private boolean status;
}
