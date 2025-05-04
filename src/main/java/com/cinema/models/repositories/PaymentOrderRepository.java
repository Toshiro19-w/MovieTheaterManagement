package com.cinema.models.repositories;

import com.cinema.models.PaymentOrder;
import com.cinema.models.repositories.Interface.IPaymentOrderRepository;

import java.util.HashMap;
import java.util.Map;

public class PaymentOrderRepository implements IPaymentOrderRepository {
    private final Map<String, PaymentOrder> paymentOrders = new HashMap<>();

    public void save(PaymentOrder paymentOrder) {
        paymentOrders.put(paymentOrder.getTransactionId(), paymentOrder);
    }

    public PaymentOrder findByTransactionId(String transactionId) {
        return paymentOrders.get(transactionId);
    }
}