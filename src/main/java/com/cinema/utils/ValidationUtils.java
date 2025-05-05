package com.cinema.utils;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtils {

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