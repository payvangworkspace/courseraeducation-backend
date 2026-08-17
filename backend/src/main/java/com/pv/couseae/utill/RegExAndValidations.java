package com.pv.couseae.utill;

import com.pv.couseae.handlers.ValidationExceptions;

import java.util.regex.Pattern;

public class RegExAndValidations {
    public  static final String IPV4_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public  static final String DOMAIN_URL = "^(https?:\\/\\/)([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:\\d{1,5})?(\\/.*)?$";
    public  static final String EMAIL_ADDRESS = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public  static final String PHONE_NUMBER = "^(?:\\+?\\d{1,4}[\\s-]?)?(?:\\(?\\d{3}\\)?[\\s-]?)?\\d{3}[\\s-]?\\d{4}$";
    public static final String PAN_SSN_REGEX = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$|^(?!000|666|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0000)\\d{4}$";
    public static final String GST_VAT_REGEX =
            "^[0-3][0-9][A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{3}$" +   // GST
                    "|^[0-9A-Z]{8,12}$";

    public static void validateIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            throw new ValidationExceptions("IP address is empty");
        }
        if (!Pattern.matches(IPV4_REGEX, ipAddress)) {
            throw new ValidationExceptions("IP address not in correct format");
        }
    }

    public static void validateDomainUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new ValidationExceptions("Domain URL is empty");
        }
        if (!Pattern.matches(DOMAIN_URL, url)) {
            throw new ValidationExceptions("Domain URL not in correct format");
        }
    }


    public static void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new ValidationExceptions("Email is empty");
        }
        if (!Pattern.matches(EMAIL_ADDRESS, email)) {
            throw new ValidationExceptions("Email not in correct format");
        }
    }

    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new ValidationExceptions("Phone number is empty");
        }
        if (!Pattern.matches(PHONE_NUMBER, phoneNumber)) {
            throw new ValidationExceptions("Phone number not in correct format");
        }
    }

    public static void validateGSTorVAT(String input) {
        if (input == null || input.isEmpty()) {
            throw new ValidationExceptions("Input is empty");
        }
        if (!Pattern.matches(GST_VAT_REGEX, input)) {
            throw new ValidationExceptions("Invalid GST or VAT format");
        }
    }

    public static void validatePANorSSN(String input) {
        if (input == null || input.isEmpty()) {
            throw new ValidationExceptions("Input is empty");
        }
        if (!Pattern.matches(PAN_SSN_REGEX, input)) {
            throw new ValidationExceptions("Invalid PAN or SSN format");
        }
    }




}
