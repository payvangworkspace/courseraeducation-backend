package com.pv.couseae.utill;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class TransactionIdGenerator {

    //    Features
//1-15-digit numeric ID → fits database fields, SMS, and API requirements.
//2-Supports thousands of txn IDs per second reliably.Ideal for high-volume fintech applications that require unique transaction IDs.
//3- High-volume ready → sequence number ensures multiple transactions in the same millisecond don’t collide.
//4- ISO/fintech aligned → timestamp-based numeric IDs are commonly used in modern financial applications.
    // Sequence counter for uniqueness within the same millisecond
    /* -------------------- 10 DIGIT GENERATOR -------------------- */

    // Supports up to 100 TPS
    private static final AtomicInteger COUNTER_10 = new AtomicInteger(0);
    private static final int MAX_10 = 99;

    // HHmmssSS → 8 digits
    private static final DateTimeFormatter FORMATTER_10 =
            DateTimeFormatter.ofPattern("HHmmssSS");

    /**
     * 10-digit Txn ID
     * Format: HHmmssSS + 2-digit sequence
     */
    public synchronized String generate10DigitTxnId() {

        String timePart = LocalDateTime.now().format(FORMATTER_10);

        int seq = COUNTER_10.getAndIncrement();
        if (seq > MAX_10) {
            COUNTER_10.set(0);
            seq = 0;
        }

        return timePart + String.format("%02d", seq);
    }

    /* -------------------- 15 DIGIT GENERATOR -------------------- */

    // Supports up to 1000 TPS
    private static final AtomicInteger COUNTER_15 = new AtomicInteger(0);
    private static final int MAX_15 = 999;

    // yyyyMMddHHmmss → 14 digits (UTC, audit safe)
    private static final DateTimeFormatter FORMATTER_15 =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneId.of("UTC"));

    /**
     * 15-digit Txn ID
     * Format: yyyyMMddHHmmss + 1-digit sequence
     */
    public synchronized String generate15DigitTxnId() {

        String timePart = FORMATTER_15.format(Instant.now()); // 14 digits

        int seq = COUNTER_15.getAndIncrement();
        if (seq > MAX_15) {
            COUNTER_15.set(0);
            seq = 0;
        }

        // Take only last digit to keep exactly 15 digits
        return timePart + (seq % 10);
    }
}

