package com.pv.couseae.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Acquirer {
    @Id
    private String acquirerId;

    @NotBlank(message = "Name should not be empty")
    @Size(max = 20, min = 2, message = "Name should be contain minimum 3 and maximum 20 digits")
    private String fullName;

    @NotBlank(message = "AcquirerModel Code should not be null")
    @NotNull(message = "AcquirerModel Code should not be null")
    @NotEmpty(message = "AcquirerModel Code should not be null")
    private String acquirerCode;

    private boolean isPayin;
    private String acquirerPgId;
    private String acquirerPgKey;
    private String acquirerPgPassword;


    private boolean isPayout;
    private String acquirerPayoutPgId;
    private String acquirerPayoutPgKey;
    private String acquirerPayoutPgPassword;

    private boolean status = true;

    private String payinWebhookUrl;
    private String payoutWebhookUrl;

    public Acquirer(String acquirerId) {
        this.acquirerId = acquirerId;
    }
}
