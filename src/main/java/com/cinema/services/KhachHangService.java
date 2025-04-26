package com.cinema.services;

import com.cinema.models.KhachHang;
import com.cinema.repositories.KhachHangRepository;
import com.cinema.utils.DatabaseConnection;

public class KhachHangService {
    private KhachHangRepository repository;

    public KhachHangService(DatabaseConnection Ba) {
        this.repository = new KhachHangRepository(Ba);
    }

    public KhachHang layThongTinKhachHang(int maKhachHang) {
        return repository.getKhachHangInfoById(maKhachHang);
    }
    
   
}
