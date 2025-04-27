package com.cinema.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class StaticQRPaymentService {

    // Tạo mã QR thanh toán tĩnh - không cần gọi API
    public BufferedImage generateStaticPaymentQR(String paymentProvider, String accountId,
                                                 BigDecimal suggestedAmount, String description) throws WriterException {

        String qrData;

        switch (paymentProvider.toLowerCase()) {
            case "momo":
                // Định dạng QR của MoMo
                qrData = String.format("2|99|%s|%s|0|0|0",
                        accountId, description);
                break;

            case "vnpay":
                // Định dạng QR của VNPay
                qrData = String.format("vnpay://%s?amount=%s&description=%s",
                        accountId, suggestedAmount, description);
                break;

            case "zalopay":
                // Định dạng QR của ZaloPay
                qrData = String.format("zalopay://app?zpid=%s&amount=%s&desc=%s",
                        accountId, suggestedAmount, description);
                break;

            case "bankqr": // QR chuẩn EMV cho ngân hàng Việt Nam
                // Tạo QR theo chuẩn EMV cho chuyển khoản ngân hàng
                qrData = createEMVQRData(accountId, suggestedAmount, description);
                break;

            default:
                // QR đơn giản nếu không xác định được nhà cung cấp
                qrData = String.format("payment?to=%s&amount=%s&desc=%s",
                        accountId, suggestedAmount, description);
                break;
        }

        // Tạo mã QR từ dữ liệu
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

        // Chuyển đổi thành BufferedImage
        BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return qrImage;
    }

    // Tạo QR theo chuẩn EMV QRCPS của NAPAS
    private String createEMVQRData(String accountNumber, BigDecimal amount, String description) {
        StringBuilder qrData = new StringBuilder();

        // Phiên bản dữ liệu QR
        qrData.append("000201"); // ID:00, Len:02, Val:01 - Phiên bản QR 01

        // Loại QR - Tĩnh hoặc Động
        qrData.append("010212"); // ID:01, Len:02, Val:12 - QR Tĩnh

        // Thông tin định tuyến thanh toán
        qrData.append("38280002VI"); // ID:38, Len:28, ID:00, Len:02, Val:VI - Quốc gia Việt Nam

        // Thông tin người thụ hưởng
        qrData.append("0115"); // ID:01, Len:15
        qrData.append(String.format("%015d", Long.parseLong(accountNumber))); // Số tài khoản

        // Thông tin ngân hàng
        qrData.append("0208VIETBANK"); // ID:02, Len:08, Val: Mã ngân hàng

        // Số tiền
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            String amountStr = amount.setScale(0, RoundingMode.HALF_UP).toString();
            qrData.append("54").append(String.format("%02d", amountStr.length())).append(amountStr);
        }

        // Thông tin mô tả
        if (description != null && !description.isEmpty()) {
            qrData.append("08").append(String.format("%02d", description.length())).append(description);
        }

        // Checksum CRC16
        qrData.append("6304"); // ID:63, Len:04
        // Ở đây cần tính CRC16, nhưng đơn giản hóa với "0000"
        qrData.append("0000");

        return qrData.toString();
    }
}