package com.cinema.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;

public class SuatChieuValidation {
    public static boolean validateShowtime(String dateTimeStr, LocalDateTime releaseDate, 
        JLabel errorLabel, ResourceBundle messages) {
        try {
            if (dateTimeStr.trim().isEmpty()) {
                ValidationUtils.showError(errorLabel, messages.getString("showtimeRequired"));
                return false;
            }

            LocalDateTime showtime = LocalDateTime.parse(dateTimeStr, 
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            
            // Kiểm tra thời gian trong quá khứ
            if (showtime.isBefore(LocalDateTime.now())) {
                ValidationUtils.showError(errorLabel, messages.getString("showtimeInPast"));
                return false;
            }
            
            // Kiểm tra thời gian không được trước ngày khởi chiếu
            if (showtime.isBefore(releaseDate)) {
                ValidationUtils.showError(errorLabel, messages.getString("showtimeBeforeRelease"));
                System.out.println("Lỗi: Suất chiếu (" + dateTimeStr + ") trước ngày khởi chiếu (" + releaseDate + ")");
                return false;
            }
            
            // Kiểm tra thời gian quá xa trong tương lai (3 tháng)
            if (showtime.isAfter(LocalDateTime.now().plusMonths(3))) {
                ValidationUtils.showError(errorLabel, messages.getString("showtimeTooFar")); 
                return false;
            }

            // Kiểm tra thời gian không được quá gần (30 phút)
            if (showtime.isBefore(LocalDateTime.now().plusMinutes(30))) {
                ValidationUtils.showError(errorLabel, messages.getString("showtimeTooClose"));
                return false;
            }
            
            ValidationUtils.hideError(errorLabel);
            System.out.println("Xác thực ngày giờ chiếu thành công: " + dateTimeStr);
            return true;
        } catch (DateTimeParseException e) {
            ValidationUtils.showError(errorLabel, messages.getString("invalidDateTimeFormat"));
            System.out.println("Lỗi định dạng ngày giờ: " + dateTimeStr);
            return false;
        }
    }

    public static boolean validateMovie(JComboBox<String> movieBox, JLabel errorLabel, ResourceBundle messages) {
        if (movieBox.getSelectedIndex() == -1) {
            ValidationUtils.showError(errorLabel, messages.getString("movieRequired"));
            return false;
        }
        ValidationUtils.hideError(errorLabel);
        return true;
    }

    public static boolean validateRoom(JComboBox<String> roomBox, JLabel errorLabel, ResourceBundle messages) {
        if (roomBox.getSelectedIndex() == -1) {
            ValidationUtils.showError(errorLabel, messages.getString("roomRequired"));
            return false;
        }
        ValidationUtils.hideError(errorLabel);
        return true;
    }
}