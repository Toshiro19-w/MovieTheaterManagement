package com.cinema.services;

import com.cinema.models.Phim;
import com.cinema.repositories.PhimRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PhimService {
    private final PhimRepository phimRepo;
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public PhimService(DatabaseConnection dbConnection) throws SQLException {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
        this.phimRepo = new PhimRepository(dbConnection);
    }

    public List<Phim> getPhimByTen(String tenPhim, String tenTheLoai, String nuocSanXuat, String daoDien) throws SQLException {
        return phimRepo.searchPhim(tenPhim, tenTheLoai, nuocSanXuat, daoDien);
    }

    public List<Phim> getAllPhim() throws SQLException {
        return phimRepo.findAll();
    }

    public List<Phim> getAllPhimDetail() throws SQLException {
        return phimRepo.findAllDetail();
    }

    public Phim addPhim(Phim phim) throws SQLException {
        // Cần lấy maTheLoai từ tenTheLoai trước khi lưu
        int maTheLoai = getMaTheLoaiByTen(phim.getTenTheLoai());
        phim.setMaTheLoai(maTheLoai);
        return phimRepo.save(phim);
    }

    public Phim updatePhim(Phim phim) throws SQLException {
        // Cần lấy maTheLoai từ tenTheLoai trước khi cập nhật
        int maTheLoai = getMaTheLoaiByTen(phim.getTenTheLoai());
        phim.setMaTheLoai(maTheLoai);
        return phimRepo.update(phim);
    }

    public boolean deletePhim(int maPhim) throws SQLException {
        phimRepo.delete(maPhim);
        return true;
    }

    // Phương thức hỗ trợ để lấy maTheLoai từ tenTheLoai
    private int getMaTheLoaiByTen(String tenTheLoai) throws SQLException {
        String sql = "SELECT maTheLoai FROM TheLoaiPhim WHERE tenTheLoai = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenTheLoai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maTheLoai");
            }
            throw new SQLException("Không tìm thấy thể loại phim: " + tenTheLoai);
        }
    }
}