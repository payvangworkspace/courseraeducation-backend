package com.pv.couseae.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "MerchantCryptoWallet")
@CompoundIndex( name = "merchant_coin_unique_idx",  def = "{'merchantId':1,'coin':1,'network':1}",  unique = true )
public class MerchantCryptoWallet {
    @Id
    private String id;   // Auto-generated ObjectId
    private String merchantId;
    private String coin;          // USDT, ETH, BTC
    private String network;     // ERC20, TRC20, BEP20, BTC, etc.
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss a")
    private LocalDateTime lastUpdated;
}
