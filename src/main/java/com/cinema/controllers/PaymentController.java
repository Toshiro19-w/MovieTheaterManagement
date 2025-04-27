package com.cinema.controllers;

import com.cinema.dto.PaymentRequest;
import com.cinema.dto.PaymentResponse;
import com.cinema.enums.PaymentStatus;
import com.cinema.services.PaymentService;

public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController() {
        this.paymentService = new PaymentService();
    }

    public PaymentResponse createPayment(PaymentRequest request, String transactionId) throws Exception {
        return paymentService.createPayment(request, transactionId);
    }

    public PaymentStatus checkPaymentStatus(String transactionId) {
        return paymentService.checkPaymentStatus(transactionId);
    }
}