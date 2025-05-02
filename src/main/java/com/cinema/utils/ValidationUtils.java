package com.cinema.utils;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtils {

    // Kiểm tra số nguyên dương
    public static boolean isPositiveInteger(int number) {
        return number > 0;
    }

    // Kiểm tra số thực dương
    public static boolean isPositiveDouble(double number) {
        return number > 0;
    }

    // Kiểm tra số BigDecimal
    public static boolean isPositiveBigDecimal(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) > 0;
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
        return phoneNumber != null && phoneNumber.matches("^0\\d{9}$");
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

    // Thêm phương thức kiểm tra định dạng ngày giờ
    public static LocalDateTime validateDateTime(String dateTimeStr, String fieldName) {
        if (!isValidString(dateTimeStr)) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " phải có định dạng dd/MM/yyyy HH:mm:ss");
        }
    }

    // Kiểm tra logic thời gian: ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc
    public static void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }
    }
}