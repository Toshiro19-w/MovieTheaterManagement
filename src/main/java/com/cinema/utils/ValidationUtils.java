package com.cinema.utils;

import java.awt.Color;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.cinema.models.repositories.SuatChieuRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.PhimService;

public class ValidationUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_MOVIE_TITLE_LENGTH = 100;
    private static final int MIN_MOVIE_DURATION = 90;
    private static final int MAX_MOVIE_DURATION = 200;

    private static final Set<String> VALID_COUNTRIES = new HashSet<>(Arrays.asList(
        "Việt Nam", "Mỹ", "Anh", "Pháp", "Đức", "Ý", "Tây Ban Nha", "Nhật Bản", "Hàn Quốc", "Trung Quốc",
        "Thái Lan", "Singapore", "Malaysia", "Indonesia", "Philippines", "Australia", "Canada", "Brazil",
        "Mexico", "Ấn Độ", "Nga", "Thụy Điển", "Na Uy", "Đan Mạch", "Hà Lan", "Bỉ", "Thụy Sĩ", "Áo",
        "New Zealand", "South Africa", "Egypt", "Turkey", "Greece", "Portugal", "Poland", "Czech Republic",
        "Hungary", "Romania", "Bulgaria", "Croatia", "Serbia", "Slovakia", "Slovenia", "Ukraine"
    ));

    public static boolean isValidString(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static boolean isValidNumber(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^0\\d{9}$");
    }

    public static boolean isValidDuration(String duration) {
        try {
            int value = Integer.parseInt(duration.trim());
            return value >= MIN_MOVIE_DURATION && value <= MAX_MOVIE_DURATION;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String validateStartDate(
        String dateStr, int maPhim, VeRepository veRepo, SuatChieuRepository suatChieuRepo, ResourceBundle messages
    ) {
        if (!isValidString(dateStr)) return messages.getString("startDateRequired");
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDate serverDate = ServerTimeService.getServerTime().toLocalDate();

            if (date.isBefore(serverDate)) {
                return messages.getString("startDateInvalid");
            }
            LocalDate maxDate = serverDate.plusYears(3);
            if (date.isAfter(maxDate)) {
                return messages.getString("startDateInvalid");
            }

            if (veRepo.hasPaidTicketsByPhim(maPhim)) {
                return messages.getString("cannotEditReleaseDateWithTickets");
            }

            if (suatChieuRepo.hasShowtimeBefore(maPhim, date)) {
                return messages.getString("releaseDateHasShowtime");
            }

            return null; // hợp lệ
        } catch (DateTimeParseException | SQLException e) {
            return messages.getString("invalidDateFormat");
        }
    }

    public static boolean isValidShowTime(String dateTimeStr, int maPhim, DatabaseConnection dbConnection) {
        if (!isValidDateTime(dateTimeStr)) return false;
        try {
            LocalDateTime showTime = parseDateTime(dateTimeStr);
            LocalDateTime serverTime = ServerTimeService.getServerTime();

            if (showTime.isBefore(serverTime)) {
                return false;
            }

            String checkReleaseDateQuery = "SELECT ngayKhoiChieu FROM Phim WHERE maPhim = ?";
            try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(checkReleaseDateQuery)) {
                pstmt.setInt(1, maPhim);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    LocalDate releaseDate = rs.getDate("ngayKhoiChieu").toLocalDate();
                    if (showTime.toLocalDate().isBefore(releaseDate)) {
                        return false;
                    }
                }
            }

            return true;
        } catch (DateTimeParseException | SQLException e) {
            return false;
        }
    }

    public static boolean isValidCountry(String country) {
        return country != null && VALID_COUNTRIES.contains(country.trim());
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9._-]{3,20}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidFullName(String fullName) {
        return isValidString(fullName);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    public static boolean isValidDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return false;
        }
        try {
            parseDateTime(dateTimeStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidDateRange(LocalDateTime start, LocalDateTime end) {
        return !end.isBefore(start);
    }

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
        return null;
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
        if (errorLabel != null) {
            errorLabel.setText(message != null ? message : "");
            errorLabel.setVisible(true);
            errorLabel.revalidate();
            errorLabel.repaint();
            System.out.println("Hiển thị lỗi: " + message + " tại " + (errorLabel.getName() != null ? errorLabel.getName() : "unknown"));
        }
    }

    public static void hideError(JLabel errorLabel) {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.revalidate();
            errorLabel.repaint();
            System.out.println("Ẩn lỗi tại " + (errorLabel.getName() != null ? errorLabel.getName() : "unknown"));
        }
    }

    public static void hideAllErrors(JLabel... errorLabels) {
        for (JLabel errorLabel : errorLabels) {
            hideError(errorLabel);
        }
    }

    public static void setErrorBorder(JComponent component) {
        if (component != null) {
            component.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
            component.revalidate();
            component.repaint();
        }
    }

    public static void setNormalBorder(JComponent component) {
        if (component != null) {
            component.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
            component.revalidate();
            component.repaint();
        }
    }

    public static JLabel createErrorLabel() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(220, 53, 69));
        label.setOpaque(false);
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

    public static void validateMovieTitleField(JTextField field, JLabel errorLabel, ResourceBundle messages, PhimService service, String currentId) {
        String title = field.getText().trim();
        try {
            if (title.isEmpty()) {
                showError(errorLabel, messages.getString("movieTitleEmpty"));
                setErrorBorder(field);
                System.out.println("Lỗi: Tên phim trống");
            } else if (title.length() > MAX_MOVIE_TITLE_LENGTH) {
                showError(errorLabel, messages.getString("movieTitleTooLong"));
                setErrorBorder(field);
                System.out.println("Lỗi: Tên phim quá dài - " + title.length() + " ký tự");
            } else if (service.isMovieTitleExists(title, currentId.isEmpty() ? 0 : Integer.parseInt(currentId))) {
                showError(errorLabel, messages.getString("movieTitleExists"));
                setErrorBorder(field);
                System.out.println("Lỗi: Tên phim đã tồn tại - " + title);
            } else {
                hideError(errorLabel);
                setNormalBorder(field);
                System.out.println("Xác thực tên phim thành công: " + title);
            }
        } catch (SQLException e) {
            showError(errorLabel, messages.getString("databaseError") + e.getMessage());
            setErrorBorder(field);
            System.out.println("Lỗi CSDL khi xác thực tên phim: " + e.getMessage());
        }
    }

    public static void validateDurationField(JTextField field, JLabel errorLabel, ResourceBundle messages) {
        String duration = field.getText().trim();
        if (duration.isEmpty()) {
            showError(errorLabel, messages.getString("durationRequired"));
            setErrorBorder(field);
            System.out.println("Lỗi: Thời lượng trống");
        } else if (!isValidDuration(duration)) {
            int value = 0;
            try {
                value = Integer.parseInt(duration);
            } catch (NumberFormatException e) {
                // Ignore
            }
            if (value < MIN_MOVIE_DURATION) {
                showError(errorLabel, "Thời lượng phim phải ít nhất " + MIN_MOVIE_DURATION + " phút");
            } else if (value > MAX_MOVIE_DURATION) {
                showError(errorLabel, "Thời lượng phim không được vượt quá " + MAX_MOVIE_DURATION + " phút");
            } else {
                showError(errorLabel, messages.getString("durationInvalid"));
            }
            setErrorBorder(field);
            System.out.println("Lỗi: Thời lượng không hợp lệ - " + duration);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
            System.out.println("Xác thực thời lượng thành công: " + duration);
        }
    }

    public static void validateCountryField(JComboBox<String> field, JLabel errorLabel, ResourceBundle messages) {
        String country = field.getSelectedItem() != null ? field.getSelectedItem().toString().trim() : "";
        if (country.isEmpty()) {
            showError(errorLabel, messages.getString("countryEmpty"));
            setErrorBorder(field);
            System.out.println("Lỗi: Nước sản xuất trống");
        } else if (!isValidCountry(country)) {
            showError(errorLabel, "Nước sản xuất không hợp lệ. Vui lòng chọn từ danh sách các quốc gia được hỗ trợ.");
            setErrorBorder(field);
            System.out.println("Lỗi: Nước sản xuất không hợp lệ - " + country);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
            System.out.println("Xác thực nước sản xuất thành công: " + country);
        }
    }

    public static void validateDateTimeField(JTextField field, JLabel errorLabel, ResourceBundle messages, String fieldName) {
        String dateTime = field.getText().trim();
        if (!isValidDateTime(dateTime)) {
            showError(errorLabel, messages.getString("invalidDateFormat"));
            setErrorBorder(field);
            System.out.println("Lỗi: Định dạng ngày giờ không hợp lệ - " + dateTime);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
            System.out.println("Xác thực ngày giờ thành công: " + dateTime);
        }
    }

    public static boolean isCustomerNameValid(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static void validateCustomerSelection(String name, JLabel errorLabel, ResourceBundle messages) {
        if (!isCustomerNameValid(name)) {
            showError(errorLabel, messages.getString("customerNameInvalid"));
            setErrorBorder(errorLabel);
        } else {
            hideError(errorLabel);
            setNormalBorder(errorLabel);
        }
    }

    public static boolean isValidSeatCode(String seat) {
        return seat != null && seat.matches("^[A-Z][1-9]$|^[A-Z]10$");
    }

    public static boolean isValidTicketPrice(String priceStr) {
        if (!isValidNumber(priceStr)) return false;
        try {
            double price = Double.parseDouble(priceStr);
            return price > 0 && price <= 500000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Set<String> getValidCountries() {
        return new HashSet<>(VALID_COUNTRIES);
    }

    public static void validateStartDateField(JTextField maPhimField, JTextField field, JLabel errorLabel, 
            ResourceBundle messages, VeRepository veRepo, SuatChieuRepository suatChieuRepo) {
        String dateStr = field.getText().trim();
        int maPhim = 0;
        
        String maPhimStr = maPhimField.getText().trim();
        if (!maPhimStr.isEmpty()) {
            try {
                maPhim = Integer.parseInt(maPhimStr);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi parse mã phim: " + maPhimStr);
            }
        }
        
        String error = validateStartDate(dateStr, maPhim, veRepo, suatChieuRepo, messages);
        if (error != null) {
            showError(errorLabel, error);
            setErrorBorder(field);
            System.out.println("Lỗi: Ngày khởi chiếu không hợp lệ - " + dateStr + " cho mã phim " + maPhim);
        } else {
            hideError(errorLabel);
            setNormalBorder(field);
            System.out.println("Xác thực ngày khởi chiếu thành công: " + dateStr + " cho mã phim " + maPhim); 
        }
    }
}