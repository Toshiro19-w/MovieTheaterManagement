package com.cinema.models.repositories.Interface;

import com.cinema.models.PaymentOrder;

public interface IPaymentOrderRepository {
    void save(PaymentOrder paymentOrder);
    PaymentOrder findByTransactionId(String transactionId);
}
