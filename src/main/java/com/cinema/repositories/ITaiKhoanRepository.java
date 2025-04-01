package com.cinema.repositories;

import com.cinema.models.TaiKhoan;
import java.util.List;

public interface ITaiKhoanRepository {
    public List<TaiKhoan> getAllTaiKhoan();
    public boolean checkUser(String tenDangNhap, String matKhau);
    public boolean dangKyTaiKhoan(TaiKhoan tk);
}
