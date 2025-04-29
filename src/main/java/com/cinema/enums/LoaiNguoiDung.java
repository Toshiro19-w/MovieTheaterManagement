package com.cinema.enums;

public enum LoaiNguoiDung {
    KHACHHANG("KhachHang"),
    NHANVIEN("NhanVien");

    private final String value;

    LoaiNguoiDung(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LoaiNguoiDung fromString(String value) {
        for (LoaiNguoiDung loaiNguoiDung : LoaiNguoiDung.values()) {
            if (loaiNguoiDung.value.equalsIgnoreCase(value)) {
                return loaiNguoiDung;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
    }
}
