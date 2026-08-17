package com.pv.couseae.enums;

public enum TransactionStatus {
    INITIATED,
    SUCCESS,
    FAILED,
    PENDING,
    CANCELLED,
    REJECTED,
    ATTEMPTED,
    AUTHENTICATED, // cards upi or payment type is corrected
    AUTHORISED,// verifies a customer has enough funds to cover the amount to be paid on a sale
    CAPTURED, // Payment has been done
    UNSETTLED,   //Unsettle transactions
    SETTLED,    //settle transactions
    PAID,
    EXPIRED,
    REFUNDED
}
