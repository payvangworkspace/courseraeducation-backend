package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebCollect360RequestDto {

    @JsonProperty("merchantRequestId")           private String merchantRequestId;
    @JsonProperty("payerVpa")                    private String payerVpa;
    @JsonProperty("payerName")                   private String payerName;
    @JsonProperty("payeeVpa")                    private String payeeVpa;
    @JsonProperty("collectRequestExpiryMinutes") private String collectRequestExpiryMinutes;
    @JsonProperty("amount")                      private String amount;
    @JsonProperty("remarks")                     private String remarks;
    @JsonProperty("purpose")                     private String purpose;
    @JsonProperty("initiationMode")              private String initiationMode;
    @JsonProperty("refUrl")                      private String refUrl;
    @JsonProperty("refCategory")                 private String refCategory;
    @JsonProperty("iat")                         private String iat;
    @JsonProperty("udfParameters")               private String udfParameters;
    @JsonProperty("invoiceName")                 private String invoiceName;
    @JsonProperty("invoiceNum")                  private String invoiceNum;
    @JsonProperty("invoiceDate")                 private String invoiceDate;
    @JsonProperty("Geocode")                     private String geocode;            // PascalCase in JSON

    @JsonProperty("mutualFundDetails")
    private List<MutualFundDetailDto> mutualFundDetails;

    @JsonProperty("payerAccountHashes")
    private List<String> payerAccountHashes;

    @JsonProperty("splitSettlementDetails")
    private SplitSettlementDetailsDto splitSettlementDetails;

    @JsonProperty("split")
    private SplitDto split;

    // ─── Mutual Fund Detail ───────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MutualFundDetailDto {
        @JsonProperty("memberId")       private String memberId;
        @JsonProperty("userId")         private String userId;
        @JsonProperty("mfPartner")      private String mfPartner;
        @JsonProperty("folioNumber")    private String folioNumber;
        @JsonProperty("orderNumber")    private String orderNumber;
        @JsonProperty("amount")         private String amount;
        @JsonProperty("schemeCode")     private String schemeCode;
        @JsonProperty("amcCode")        private String amcCode;
        @JsonProperty("panNumber")      private String panNumber;
        @JsonProperty("investmentType") private String investmentType;
    }

    // ─── Split Settlement ─────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SplitSettlementDetailsDto {
        @JsonProperty("splitType")     private String splitType;
        @JsonProperty("merchantSplit") private String merchantSplit;

        @JsonProperty("partnersSplit")
        private List<PartnerSplitDto> partnersSplit;

        @Data @Builder @NoArgsConstructor @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PartnerSplitDto {
            @JsonProperty("partnerId") private String partnerId;
            @JsonProperty("value")     private String value;
        }
    }

    // ─── Split ────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SplitDto {
        @JsonProperty("name")  private String name;
        @JsonProperty("value") private String value;
    }
}