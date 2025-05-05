package com.cinema.services;

import com.cinema.models.TaiKhoan;
import com.cinema.models.repositories.TaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanService(DatabaseConnection databaseConnection) {
        this.taiKhoanRepository = new TaiKhoanRepository(databaseConnection);
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
}
