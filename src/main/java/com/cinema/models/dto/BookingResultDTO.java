package com.cinema.models.dto;

import java.math.BigDecimal;

import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;

/**
 * DTO chứa thông tin kết quả của quá trình đặt vé
 */
public record BookingResultDTO(
    SuatChieu suatChieu,
    Ghe ghe,
    BigDecimal giaVe,
    String transactionId
) {
    // Record tự động tạo constructor, getters, equals, hashCode và toString
}