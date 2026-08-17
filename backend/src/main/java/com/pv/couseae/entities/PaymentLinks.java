package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pv.couseae.utill.DoubleToTwoDecimalSerializer;
import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class PaymentLinks extends Tracker {
    @Id
    private String PaymentLinkId;
    private String PaymentLinkUrl;

    private String orderId;

    private String status;

    private String merchantId;

    @JsonSerialize(using = DoubleToTwoDecimalSerializer.class)
    private double payableAmount;

    private String countryCode;

    private String currencyCode;

    private String customerName;

    private String customerEmailId;

    private String customerContactNumber;

    private String notifyEmail;
    private String notifyPhone;
    private String qrCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private LocalDateTime notifyDate;

    @Transient
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    private Date expDate;


}
