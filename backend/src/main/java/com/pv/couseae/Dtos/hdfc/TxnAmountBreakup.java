package com.pv.couseae.Dtos.hdfc;

import lombok.Data;

@Data
public class TxnAmountBreakup {
    private String name;
    private double amount;
    private int sno;
    private String method;
}