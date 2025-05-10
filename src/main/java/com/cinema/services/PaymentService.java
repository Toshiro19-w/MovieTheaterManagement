package com.cinema.services;

import java.awt.image.BufferedImage;

import com.cinema.enums.PaymentStatus;
import com.cinema.models.PaymentOrder;
import com.cinema.models.dto.PaymentRequest;
import com.cinema.models.dto.PaymentResponse;
import com.cinema.models.repositories.PaymentOrderRepository;
import com.cinema.utils.QrCodeGenerator;

public class PaymentService {
    private final PaymentOrderRepository paymentOrderRepository;

    public PaymentService() {
        this.paymentOrderRepository = new PaymentOrderRepository();
    }

    public PaymentResponse createPayment(PaymentRequest request, String transactionId) throws Exception {
        PaymentOrder paymentOrder = new PaymentOrder(
                transactionId,
                request.getAmount(),
                request.getDescription(),
                request.getPaymentMethod()
        );

        paymentOrderRepository.save(paymentOrder);

        BufferedImage qrImage = QrCodeGenerator.generateQRCode(
                request.getPaymentMethod().toString().toLowerCase(),
                request.getAccountId(),
                request.getAmount(),
                request.getDescription()
        );

        return new PaymentResponse(
                transactionId,
                qrImage,
                paymentOrder.getStatus(),
                "Tạo mã QR thành công"
        );
    }

    public PaymentStatus checkPaymentStatus(String transactionId) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByTransactionId(transactionId);
        if (paymentOrder == null) {
            return PaymentStatus.FAILED;
        }
        paymentOrder.setStatus(PaymentStatus.COMPLETED);
        paymentOrderRepository.save(paymentOrder);
        return paymentOrder.getStatus();
    }
}