package com.cinema.services;

import com.cinema.repositories.TaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanService(DatabaseConnection databaseConnection) {
        this.taiKhoanRepository = new TaiKhoanRepository(databaseConnection);
    }

    public void saveResetTokenToDB(String email, String token) {
        taiKhoanRepository.saveResetTokenToDB(email, token);
    }

    public boolean checkEmailExists(String email) {
        return taiKhoanRepository.checkEmailExists(email);
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

        // Check if username or maNguoiDung already exists
        if (taiKhoanRepository.existsByTenDangNhap(taiKhoan.getTenDangNhap())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (taiKhoanRepository.existsByMaNguoiDung(taiKhoan.getMaNguoiDung())) {
            throw new IllegalArgumentException("Nhân viên này đã có tài khoản");
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(taiKhoan.getMatKhau(), BCrypt.gensalt());
        taiKhoan.setMatKhau(hashedPassword);

        // Create account
        taiKhoanRepository.createTaiKhoan(taiKhoan);
    }
}
