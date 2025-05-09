package com.cinema.utils;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

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

    // Thêm phương thức kiểm tra tên người dùng hợp lệ
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9._-]{3,20}$");
    }

    // Thêm phương thức kiểm tra mật khẩu hợp lệ
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    // Kiểm tra tên đầy đủ
    public static boolean isValidFullName(String fullName) {
        return isValidString(fullName);
    }

    // Kiểm tra và trả về thông báo lỗi cho username
    public static String validateUserInput(String username, String fullName, String phone, String email, String password, String confirmPassword) {
        if (!isValidUsername(username)) {
            return "Username không hợp lệ";
        }
        if (!isValidFullName(fullName)) {
            return "Tên đầy đủ không được để trống";
        }
        if (!isValidPhoneNumber(phone)) {
            return "Số điện thoại không hợp lệ";
        }
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        if (!isValidPassword(password)) {
            return "Mật khẩu phải có ít nhất 8 ký tự";
        }
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp";
        }
        return null; // Trả về null nếu không có lỗi
    }

    public static String validateForgotPasswordInput(String username, String email, String phone, String newPassword) {
        if (!isValidUsername(username)) {
            return "Username không hợp lệ";
        }
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        if (!isValidPhoneNumber(phone)) {
            return "Số điện thoại không hợp lệ";
        }
        if (!isValidPassword(newPassword)) {
            return "Mật khẩu mới phải có ít nhất 8 ký tự";
        }
        return null;
    }

    public static String validateLoginInput(String username, String password) {
        if (!isValidUsername(username)) {
            return "Username không hợp lệ";
        }
        if (!isValidPassword(password)) {
            return "Mật khẩu không hợp lệ";
        }
        return null;
    }

    public static void showError(JLabel errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public static void hideError(JLabel errorLabel) {
        errorLabel.setVisible(false);
    }

    public static void setErrorBorder(JComponent component) {
        component.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69)));
    }

    public static void setNormalBorder(JComponent component) {
        component.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
    }

    public static JLabel createErrorLabel() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);
        label.setBackground(new Color(220, 53, 69));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setVisible(false);
        return label;
    }

    public static void validateUsernameField(JTextField field, JLabel errorLabel, ResourceBundle messages) {
        String username = field.getText().trim();
        if (username.isEmpty()) {
            showError(errorLabel, messages.getString("usernameEmpty"));
            setErrorBorder(field);
        } else if (!isValidUsername(username)) {
            showError(errorLabel, messages.getString("usernameInvalid"));
            setErrorBorder(field);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
        }
    }

    public static void validateFullNameField(JTextField field, JLabel errorLabel, ResourceBundle messages) {
        String fullName = field.getText().trim();
        if (!isValidFullName(fullName)) {
            showError(errorLabel, messages.getString("fullNameEmpty"));
            setErrorBorder(field);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
        }
    }

    public static void validatePhoneField(JTextField field, JLabel errorLabel, ResourceBundle messages) {
        String phone = field.getText().trim();
        if (!isValidPhoneNumber(phone)) {
            showError(errorLabel, messages.getString("phoneInvalid"));
            setErrorBorder(field);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
        }
    }

    public static void validateEmailField(JTextField field, JLabel errorLabel, ResourceBundle messages) {
        String email = field.getText().trim();
        if (!isValidEmail(email)) {
            showError(errorLabel, messages.getString("emailInvalid"));
            setErrorBorder(field);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
        }
    }

    public static void validatePasswordField(JPasswordField field, JLabel errorLabel, ResourceBundle messages) {
        String password = new String(field.getPassword()).trim();
        if (!isValidPassword(password)) {
            showError(errorLabel, messages.getString("passwordInvalid"));
            setErrorBorder(field);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
        }
    }

    public static void validateConfirmPasswordField(JPasswordField passwordField, JPasswordField confirmField, 
            JLabel errorLabel, ResourceBundle messages) {
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmField.getPassword()).trim();
        if (!password.equals(confirmPassword)) {
            showError(errorLabel, messages.getString("confirmPasswordMismatch"));
            setErrorBorder(confirmField);
        } else {
            hideError(errorLabel);
            setNormalBorder(confirmField);
        }
    }

    public static void validateLoginFields(JTextField usernameField, JPasswordField passwordField,
                                        JLabel errorLabel, ResourceBundle messages) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError(errorLabel, messages.getString("loginEmpty"));
            setErrorBorder(username.isEmpty() ? usernameField : passwordField);
        } else if (!isValidUsername(username)) {
            showError(errorLabel, messages.getString("usernameInvalid"));
            setErrorBorder(usernameField);
        } else {
            hideError(errorLabel);
            setNormalBorder(usernameField);
            setNormalBorder(passwordField);
        }
    }
}