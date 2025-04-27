package com.cinema.enums;

public enum PaymentStatus {
    PENDING,    // Đang chờ thanh toán
    COMPLETED,  // Thanh toán thành công
    FAILED,     // Thanh toán thất bại
    EXPIRED     // Hết thời gian thanh toán
}