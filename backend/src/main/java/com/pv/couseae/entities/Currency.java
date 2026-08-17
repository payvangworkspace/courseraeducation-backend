package com.pv.couseae.entities;

import com.pv.couseae.utill.Tracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document()
public class Currency extends Tracker {
    @Id
    private String currencyId;

    private String currencyName;
    private String currencyCode;
    private int currencyDecimalPlace;
    private String symbol;

    public Currency(String currencyId) {
        this.currencyId = currencyId;
    }
}
