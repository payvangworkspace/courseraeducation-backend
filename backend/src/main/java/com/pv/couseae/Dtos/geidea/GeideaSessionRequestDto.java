package com.pv.couseae.Dtos.geidea;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Geidea Create Session request body.
 *
 * NON_NULL so optional blocks are omitted rather than sent as null — some Geidea payment
 * methods reject explicit nulls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeideaSessionRequestDto {

    /** Mandatory. Exactly two decimals, and must match what was signed. */
    private BigDecimal amount;

    /** Mandatory. AED for UAE; others require multicurrency on the account. No INR. */
    private String currency;

    /** Mandatory. Must be the identical string used to build the signature. */
    private String timestamp;

    /** Mandatory. HMAC-SHA256, base64. */
    private String signature;

    /** Mandatory. HTTPS with a valid certificate. */
    private String callbackUrl;

    /** Your own reference. Optional to Geidea, but set it — it is your only join key. */
    private String merchantReferenceId;

    /** Browser redirect after completion. Mandatory for Tabby. */
    private String returnUrl;

    /** "en" or "ar". Mandatory for Tamara. */
    private String language;

    /** Defaults to "Pay" when omitted. */
    private String paymentOperation;

    /** True stores the card and returns a tokenId on the callback. */
    private Boolean cardOnFile;

    /** Existing card token for a tokenized payment. */
    private String tokenId;

    private CofAgreement cofAgreement;
    private Customer customer;
    private Order order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CofAgreement {
        private String id;
        /** "Unscheduled" or "Recurring". */
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Customer {
        private String email;
        private String phoneNumber;
        private String phonecountrycode;
        private String firstName;
        private String lastName;
        private Addresses address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Addresses {
        private Address billing;
        private Address shipping;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address {
        /** Three-letter country code, e.g. ARE. */
        private String country;
        private String city;
        private String street;
        private String postalCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Order {
        private List<Item> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        private String merchantItemId;
        private String name;
        private String description;
        private String categories;
        private Integer count;
        private BigDecimal price;
        private String sku;
    }
}
