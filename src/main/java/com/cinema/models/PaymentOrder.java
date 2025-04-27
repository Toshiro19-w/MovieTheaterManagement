package com.cinema.models;

import com.cinema.enums.PaymentMethod;
import com.cinema.enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentOrder {
    private String transactionId;
    private BigDecimal amount;
    private String description;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    public PaymentOrder(String transactionId, BigDecimal amount, String description, PaymentMethod paymentMethod) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    // Getters và Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}