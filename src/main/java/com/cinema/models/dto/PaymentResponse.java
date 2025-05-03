package com.cinema.models.dto;

import com.cinema.enums.PaymentStatus;

import java.awt.image.BufferedImage;

public class PaymentResponse {
    private String transactionId;
    private BufferedImage qrImage;
    private PaymentStatus status;
    private String message;

    public PaymentResponse(String transactionId, BufferedImage qrImage, PaymentStatus status, String message) {
        this.transactionId = transactionId;
        this.qrImage = qrImage;
        this.status = status;
        this.message = message;
    }

    // Getters và Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BufferedImage getQrImage() {
        return qrImage;
    }

    public void setQrImage(BufferedImage qrImage) {
        this.qrImage = qrImage;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}