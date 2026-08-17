package com.pv.couseae.enums;

public enum TransactionTypes {
    ORDER,          //Create Order
    SENTTOBANK,     // failed
    ATTEMPTED,      // Checkout attempt
    SETTLEMENT,     // Settled to merchant
    REFUND,         // refund proceeded to customer's account
    FAILED,
    SUCCESS         // Payout transaction completed
}
