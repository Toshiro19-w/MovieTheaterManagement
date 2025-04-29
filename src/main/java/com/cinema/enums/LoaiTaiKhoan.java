package com.cinema.enums;

public enum LoaiTaiKhoan {
    ADMIN("Admin"),
    USER("User"),
    QUANLYPHIM("QuanLyPhim"),
    THUNGAN("ThuNgan"),
    BANVE("BanVe");

    private final String value;

    LoaiTaiKhoan(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LoaiTaiKhoan fromString(String value) {
        for (LoaiTaiKhoan loaiTaiKhoan : LoaiTaiKhoan.values()) {
            if (loaiTaiKhoan.value.equalsIgnoreCase(value)) {
                return loaiTaiKhoan;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
    }
}
