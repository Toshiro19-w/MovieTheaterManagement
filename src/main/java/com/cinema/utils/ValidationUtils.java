package com.cinema.utils;

import com.cinema.models.*;

import java.math.BigDecimal;

public class ValidationUtils {

    // Kiểm tra số nguyên dương
    public static boolean isPositiveInteger(int number) {
        return number > 0;
    }

    // Kiểm tra số thực dương
    public static boolean isPositiveDouble(double number) {
        return number > 0;
    }

    // Kiểm tra chuỗi không rỗng và không chỉ chứa khoảng trắng
    public static boolean isValidString(String input) {
        return input != null && !input.trim().isEmpty();
    }

    // Kiểm tra email hợp lệ
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    // Kiểm tra số điện thoại hợp lệ (10 chữ số, bắt đầu từ 0)
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^0d{9}$");
    }

    // Kiểm tra mã suất chiếu hợp lệ (chỉ chứa chữ và số, tối đa 10 ký tự)
    public static boolean isValidShowCode(String code) {
        return code != null && code.matches("^[A-Za-z0-9]{1,10}$");
    }

    public static void validatePagination(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("Số trang phải lớn hơn 0");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Kích thước trang phải từ 1 đến 100");
        }
    }

    public static void validatePositive(int value, String errorMessage) {
        if (value <= 0) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void validateNotNull(Object obj, String errorMessage) {
        if (obj == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static boolean validateVe(Ve ve) {
        validateNotNull(ve, "Vé không được null");

        if (ve.getSoGhe() == null || ve.getSoGhe().trim().isEmpty()) {
            throw new IllegalArgumentException("Số ghế không được để trống");
        }

        if (ve.getGiaVe() == null || ve.getGiaVe().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá vé phải lớn hơn hoặc bằng 0");
        }

        if (ve.getTrangThai() != null && !isValidTrangThai(ve.getTrangThai())) {
            throw new IllegalArgumentException("Trạng thái vé không hợp lệ");
        }
        return false;
    }

    private static boolean isValidTrangThai(TrangThaiVe trangThai) {
        for (TrangThaiVe tt : TrangThaiVe.values()) {
            if (tt == trangThai) {
                return true;
            }
        }
        return false;
    }
}
