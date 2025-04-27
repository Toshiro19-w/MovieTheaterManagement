package com.cinema.models.repositories;

import com.cinema.models.PaymentOrder;

import java.util.HashMap;
import java.util.Map;

public class PaymentOrderRepository {
    private final Map<String, PaymentOrder> paymentOrders = new HashMap<>();

    public void save(PaymentOrder paymentOrder) {
        paymentOrders.put(paymentOrder.getTransactionId(), paymentOrder);
    }

    public PaymentOrder findByTransactionId(String transactionId) {
        return paymentOrders.get(transactionId);
    }
}