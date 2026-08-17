package com.pv.couseae.Dtos.Crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    private Optional<String> txHash = Optional.empty();
    private Optional<Integer> blockConfirmations = Optional.empty();
    private Optional<Instant> txTimestamp = Optional.empty();
}
