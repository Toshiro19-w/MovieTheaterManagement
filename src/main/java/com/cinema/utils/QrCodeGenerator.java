package com.cinema.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class QrCodeGenerator {
    public static BufferedImage generateQRCode(String paymentProvider, String accountId,
                                               BigDecimal amount, String description) throws WriterException {
        String qrData = switch (paymentProvider.toLowerCase()) {
            case "momo" -> String.format("2|99|%s|%s|0|0|0", accountId, description);
            case "vnpay" -> String.format("vnpay://%s?amount=%s&description=%s", accountId, amount, description);
            case "zalopay" -> String.format("zalopay://app?zpid=%s&amount=%s&desc=%s", accountId, amount, description);
            case "bankqr" -> createEMVQRData(accountId, amount, description);
            default -> String.format("payment?to=%s&amount=%s&desc=%s", accountId, amount, description);
        };

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

        BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return qrImage;
    }

    private static String createEMVQRData(String accountNumber, BigDecimal amount, String description) {
        StringBuilder qrData = new StringBuilder();

        qrData.append("000201");
        qrData.append("010212");
        qrData.append("38280002VI");
        qrData.append("0115");
        qrData.append(String.format("%015d", Long.parseLong(accountNumber)));
        qrData.append("0208VIETBANK");

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            String amountStr = amount.setScale(0, RoundingMode.HALF_UP).toString();
            qrData.append("54").append(String.format("%02d", amountStr.length())).append(amountStr);
        }

        if (description != null && !description.isEmpty()) {
            qrData.append("08").append(String.format("%02d", description.length())).append(description);
        }

        qrData.append("6304");
        qrData.append("0000");

        return qrData.toString();
    }
}