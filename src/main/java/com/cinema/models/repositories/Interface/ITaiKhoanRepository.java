package com.cinema.models.repositories.Interface;

import com.cinema.models.TaiKhoan;

import java.sql.SQLException;

public interface ITaiKhoanRepository {
    void saveResetTokenToDB(String email, String token);
    boolean checkEmailExists(String email);
    void createTaiKhoan(TaiKhoan taiKhoan) throws SQLException;
    boolean existsByTenDangNhap(String tenDangNhap) throws SQLException;
    boolean existsByMaNguoiDung(Integer maNguoiDung) throws SQLException;
}
