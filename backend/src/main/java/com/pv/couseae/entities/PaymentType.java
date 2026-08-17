package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pv.couseae.utill.Tracker;
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

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document()
public class PaymentType extends Tracker {
    @Id
    private String paymentTypeId;

    @NotBlank(message = "Payment Type Name should not be empty")
    @NotNull(message = "Payment Type Name should not be empty")
    @NotEmpty(message = "Payment Type Name should not be empty")
    private String paymentTypeName;
    @NotBlank(message = "Payment Type Code should not be empty")
    @NotNull(message = "Payment Type Code should not be empty")
    @NotEmpty(message = "Payment Type Code should not be empty")
    private String paymentTypeCode;

    @DBRef
    @JsonIgnoreProperties({"createdDate","createdBy","lastModifiedDate","lastModifiedBy","status","countryShortName2","countryNumericCode","countrySubregion"})
    private LocationCountry country;

    @DBRef
    @JsonIgnoreProperties({"createdDate","createdBy","lastModifiedDate","lastModifiedBy","status",})
    private Currency currency;

    public PaymentType(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }
}
