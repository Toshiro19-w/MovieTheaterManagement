package com.cinema.models;

public enum VaiTro {
    ADMIN("Admin"),
    QUANLY("QuanLy"),
    THUNGAN("ThuNgan"),
    BANVE("BanVe");

    private final String value;

    VaiTro(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static VaiTro fromString(String value) {
        for (VaiTro vaiTro : VaiTro.values()) {
            if (vaiTro.value.equalsIgnoreCase(value)) {
                return vaiTro;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
    }
}
