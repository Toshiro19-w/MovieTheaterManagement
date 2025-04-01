package com.cinema.repositories;

import com.cinema.models.KhachHang;
import java.util.List;

public interface IKhachHangRepository {
    public List<KhachHang> getAllKhachHang();
    public int getIdByEmail(String email);
    public static boolean login(String email, String matKhau) {
        return false;
    }
}
