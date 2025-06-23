package com.cinema.models.dto;

import java.math.BigDecimal;

import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;

/**
 * DTO chứa thông tin kết quả của quá trình thanh toán
 */
public class PaymentResult {
    private final SuatChieu suatChieu;
    private final Ghe ghe;
    private final BigDecimal giaVe;
    private final String transactionId;

    public PaymentResult(SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, String transactionId) {
        this.suatChieu = suatChieu;
        this.ghe = ghe;
        this.giaVe = giaVe;
        this.transactionId = transactionId;
    }

    public SuatChieu getSuatChieu() {
        return suatChieu;
    }

    public Ghe getGhe() {
        return ghe;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public String getTransactionId() {
        return transactionId;
    }
}