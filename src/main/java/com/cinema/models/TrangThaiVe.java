package com.cinema.models;

public enum TrangThaiVe {
    AVAILABLE("available"),
    BOOKED("booked"),
    PAID("paid"),
    CANCELLED("cancelled");

    private final String value;

    TrangThaiVe(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static TrangThaiVe fromString(String value) {
        for (TrangThaiVe trangThai : TrangThaiVe.values()) {
            if (trangThai.value.equalsIgnoreCase(value)) {
                return trangThai;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
    }
}
