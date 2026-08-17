package com.pv.couseae.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactionCounters")
public class TransactionCounter {
    @Id
    private String id;

    private String merchantId;
    private String userId;   // keep if you want per-user limits
    private String txnType;

    @Builder.Default
    private DailyCounter daily = new DailyCounter();

    @Builder.Default
    private MonthlyCounter monthly = new MonthlyCounter();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCounter {
        private String date; // e.g. "2025-10-03"
        @Field(targetType = FieldType.DOUBLE)
        private BigDecimal amount = BigDecimal.ZERO;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyCounter {
        private String month; // e.g. "2025-10"
        @Field(targetType = FieldType.DOUBLE)
        private BigDecimal amount = BigDecimal.ZERO;
    }
}
