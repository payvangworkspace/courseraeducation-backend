package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Document(collection = "ipapikeyInfo")
// Uniqueness now lives on the (merchantId, allowedIps) PAIR, not on merchantId alone.
// This allows: one merchant → many IPs, AND one IP → many merchants,
// while still preventing an exact duplicate (same merchant + same IP) twice.
@CompoundIndex(name = "unique_merchant_ip", def = "{'merchantId': 1, 'allowedIps': 1}", unique = true)
public class IpApiKeyInfo {

    @Id
    private String id;              // generated Mongo _id — no longer the merchantId

    @Indexed                        // non-unique index so findByMerchantId is fast
    private String merchantId;      // many rows can share the same merchantId now

    private String keyHash;         // bcrypt or PBKDF2
    private String allowedIps;      // single IP per row (see note below)
    private Boolean active = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy  hh:mm:ss a")
    @CreatedDate
    private LocalDateTime createdDate;
}