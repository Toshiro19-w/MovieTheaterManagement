package com.cinema.services;

import com.cinema.dto.PaymentRequest;
import com.cinema.dto.PaymentResponse;
import com.cinema.enums.PaymentStatus;
import com.cinema.models.PaymentOrder;
import com.cinema.models.repositories.PaymentOrderRepository;
import com.cinema.utils.QrCodeGenerator;

import java.awt.image.BufferedImage;

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

        // Mô phỏng kiểm tra trạng thái: sau 10 giây, giao dịch thành công
        try {
            Thread.sleep(10000);
            paymentOrder.setStatus(PaymentStatus.COMPLETED);
            paymentOrderRepository.save(paymentOrder);
            return PaymentStatus.COMPLETED;
        } catch (InterruptedException e) {
            paymentOrder.setStatus(PaymentStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return PaymentStatus.FAILED;
        }
    }
}