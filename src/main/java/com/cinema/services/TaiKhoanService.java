package com.cinema.services;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.cinema.models.TaiKhoan;
import com.cinema.models.repositories.TaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;

public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanService(DatabaseConnection databaseConnection) {
        this.taiKhoanRepository = new TaiKhoanRepository(databaseConnection);
    }
    
    public String findUsernameByEmailOrPhone(String emailOrPhone) throws SQLException {
        return taiKhoanRepository.findUsernameByEmailOrPhone(emailOrPhone);
    }

    public void createTaiKhoan(TaiKhoan taiKhoan) throws SQLException {
        // Validate input
        if (taiKhoan.getTenDangNhap() == null || taiKhoan.getTenDangNhap().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (taiKhoan.getMatKhau() == null || taiKhoan.getMatKhau().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (taiKhoan.getMaNguoiDung() == null) {
            throw new IllegalArgumentException("Mã người dùng không được để trống");
        }

        // Kiểm tra nếu tài khoản có tồi tại
        if (taiKhoanRepository.existsByTenDangNhap(taiKhoan.getTenDangNhap())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (taiKhoanRepository.existsByMaNguoiDung(taiKhoan.getMaNguoiDung())) {
            throw new IllegalArgumentException("Nhân viên này đã có tài khoản");
        }

        // mã hoá mật khẩu
        String hashedPassword = BCrypt.hashpw(taiKhoan.getMatKhau(), BCrypt.gensalt());
        taiKhoan.setMatKhau(hashedPassword);

        // tạo mật khẩu
        taiKhoanRepository.createTaiKhoan(taiKhoan);
    }

    public boolean verifyUserForPasswordReset(String username, String email, String phone) throws SQLException {
        return taiKhoanRepository.verifyUser(username, email, phone);
    }

    public boolean updatePassword(String username, String hashedPassword) throws SQLException {
        return taiKhoanRepository.updatePassword(username, hashedPassword);
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        if (!ValidationUtils.isValidUsername(username) || !ValidationUtils.isValidPassword(password)) {
            return false;
        }
        return taiKhoanRepository.authenticateUser(username, password);
    }

    public TaiKhoan getTaiKhoanByUsername(String username) throws SQLException {
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("Username không hợp lệ");
        }
        return taiKhoanRepository.findByUsername(username);
    }

    public int getUserIdFromUsername(String username) throws SQLException {
        return taiKhoanRepository.getUserIdFromUsername(username);
    }

    public int handleRegistration(String username, String fullName, String phone, String email, String password) throws SQLException {
        // Validate input
        if (!ValidationUtils.isValidString(username) || !ValidationUtils.isValidString(fullName) ||
            !ValidationUtils.isValidPhoneNumber(phone) || !ValidationUtils.isValidEmail(email) ||
            !ValidationUtils.isValidPassword(password)) {
            throw new IllegalArgumentException("Dữ liệu đầu vào không hợp lệ");
        }

        // Check if username already exists
        if (taiKhoanRepository.existsByTenDangNhap(username)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        return taiKhoanRepository.registerUser(username, fullName, phone, email, password);
    }

    public String getEmailByUsername(String username) throws SQLException {
        return taiKhoanRepository.getEmailByUsername(username);
    }
}
