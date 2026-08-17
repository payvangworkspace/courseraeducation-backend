package com.pv.couseae.utill;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestCrypto {
    private int start;
    private int size;
    private String currencyCode = "";

    private String keyword = "";
    private String status = "";
    private String type = "";

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy" ,locale = "en")
    private Date dateFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy",locale = "en")
    private Date dateTo;

    private String userName = "";
    private String paymentTypeId = "";

    private String orderId = "";
    private String transactionId = "";

    private String cryptoType = "";
    private String networkType = "";
    private String utr = "";

    public int getSize() {
        if (size > 25 || size < 1)
            return 25;
        return size;
    }

    public Date getDateFrom() {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        if (this.dateFrom == null) {
            LocalDate today = LocalDate.now(zoneId);
            // Set time to 00:00:00
            LocalDateTime startOfDay = today.atStartOfDay();
            // Convert to Date
            System.out.println("dateFrom " + Date.from(startOfDay.atZone(zoneId).toInstant()));
            return Date.from(startOfDay.atZone(zoneId).toInstant());
        }
        Instant instant = dateFrom.toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        // Set time to 00:00:00
        LocalDateTime startOfDay = localDate.atStartOfDay();
        System.out.println("dateFrom " + Date.from(startOfDay.atZone(zoneId).toInstant()));
        return Date.from(startOfDay.atZone(zoneId).toInstant());
    }

    public Date getDateTo() {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        if (dateTo == null) {
            LocalDate today = LocalDate.now(zoneId);
            // Set time to 00:00:00
            LocalDateTime endOfDay = today.atTime(23, 59, 59, 9999999);
            // Convert to Date
            System.out.println("dateTo " + Date.from(endOfDay.atZone(zoneId).toInstant()));
            return Date.from(endOfDay.atZone(zoneId).toInstant());
        }
        Instant instant = dateTo.toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        // Set time to 00:00:00
        LocalDateTime endOfDay = localDate.atTime(23, 59, 59, 9999999);
        System.out.println("dateTo " + Date.from(endOfDay.atZone(zoneId).toInstant()));
        return Date.from(endOfDay.atZone(zoneId).toInstant());
    }
}
