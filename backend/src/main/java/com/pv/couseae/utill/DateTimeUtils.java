package com.pv.couseae.utill;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {
    public static String dateToken() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh:mm:ss:SSS");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now).replaceAll("-","").replaceAll(":","");
    }
    public static Date getDateFrom(Date dateFrom) {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        if (dateFrom == null) {
            LocalDate today = LocalDate.now(zoneId);
            // Set time to 00:00:00
            LocalDateTime startOfDay = today.atStartOfDay();
            // Convert to Date
            System.out.println("dateFrom "+Date.from(startOfDay.atZone(zoneId).toInstant()));
            return Date.from(startOfDay.atZone(zoneId).toInstant());
        }
        Instant instant = dateFrom.toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        // Set time to 00:00:00
        LocalDateTime startOfDay = localDate.atStartOfDay();
        System.out.println("dateFrom "+Date.from(startOfDay.atZone(zoneId).toInstant()));
        return Date.from(startOfDay.atZone(zoneId).toInstant());
    }

    public static Date getDateTo(Date dateTo) {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        if (dateTo == null){
            LocalDate today = LocalDate.now(zoneId);
            // Set time to 00:00:00
            LocalDateTime endOfDay = today.atTime(23,59,59,9999999);
            // Convert to Date
            System.out.println("dateTo "+Date.from(endOfDay.atZone(zoneId).toInstant()));
            return Date.from(endOfDay.atZone(zoneId).toInstant());
        }
        Instant instant = dateTo.toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        // Set time to 00:00:00
        LocalDateTime endOfDay = localDate.atTime(23,59,59,9999999);
        System.out.println("dateTo "+Date.from(endOfDay.atZone(zoneId).toInstant()));
        return Date.from(endOfDay.atZone(zoneId).toInstant());
    }



}
