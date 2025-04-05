package com.cinema.services;

import com.cinema.models.NhanVien;
import com.cinema.repositories.NhanVienRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;

    public NhanVienService(DatabaseConnection databaseConnection) {
        this.nhanVienRepository = new NhanVienRepository(databaseConnection);
    }

    public List<NhanVien> findAllNhanVien() throws SQLException {
        return nhanVienRepository.findAll();
    }

    public void saveNhanVien(NhanVien entity) throws SQLException {
        nhanVienRepository.save(entity);
    }

    public void updateNhanVien(NhanVien entity) throws SQLException {
        nhanVienRepository.update(entity);
    }

    public void deleteNhanVien(int maNguoiDung) throws SQLException {
        nhanVienRepository.delete(maNguoiDung);
    }
}
