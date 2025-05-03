package com.cinema.models.dto;

import com.cinema.enums.PaymentMethod;

import java.math.BigDecimal;

public class PaymentRequest {
    private String accountId;
    private BigDecimal amount;
    private String description;
    private PaymentMethod paymentMethod;

    public PaymentRequest(String accountId, BigDecimal amount, String description, PaymentMethod paymentMethod) {
        this.accountId = accountId;
        this.amount = amount;
        this.description = description;
        this.paymentMethod = paymentMethod;
    }

    // Getters và Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}