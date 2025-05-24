package com.cinema.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.cinema.models.Phim;
import com.cinema.models.repositories.PhimRepository;
import com.cinema.utils.DatabaseConnection;

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

    public List<Phim> getAllPhim() throws SQLException {
        return phimRepo.findAll();
    }

    public Phim getPhimById(int maPhim) throws SQLException {
        return phimRepo.findById(maPhim);
    }

    public Phim addPhim(Phim phim) throws SQLException {
        int maTheLoai = phimRepo.getMaTheLoaiByTen(phim.getTenTheLoai());
        phim.setMaTheLoai(maTheLoai);
        return phimRepo.save(phim);
    }

    public Phim updatePhim(Phim phim) throws SQLException {
        // Kiểm tra xem phim có tồn tại không
        Phim existingPhim = phimRepo.findById(phim.getMaPhim());
        if (existingPhim == null) {
            throw new SQLException("Không tìm thấy phim với mã: " + phim.getMaPhim());
        }

        // Kiểm tra tên phim có bị trùng không (trừ phim hiện tại)
        if (phimRepo.isMovieTitleExists(phim.getTenPhim(), phim.getMaPhim())) {
            throw new SQLException("Tên phim đã tồn tại!");
        }

        int maTheLoai = phimRepo.getMaTheLoaiByTen(phim.getTenTheLoai());
        phim.setMaTheLoai(maTheLoai);
        return phimRepo.update(phim);
    }

    public boolean deletePhim(int maPhim) throws SQLException {
        phimRepo.delete(maPhim);
        return true;
    }

    public boolean isMovieTitleExists(String tenPhim, int excludeMaPhim) throws SQLException {
        return phimRepo.isMovieTitleExists(tenPhim, excludeMaPhim);
    }

    // Lấy danh sách thể loại duy nhất
    public List<String> getAllTheLoai() throws SQLException {
        return phimRepo.getAllTheLoai();
    }

    // Lấy danh sách trạng thái duy nhất
    public List<String> getAllTrangThai() throws SQLException {
        return phimRepo.getAllTrangThai();
    }

    // Lấy danh sách định dạng duy nhất
    public List<String> getAllDinhDang() throws SQLException {
        return phimRepo.getAllDinhDang();
    }

    public List<Phim> getPhimByTenPhong(String tenPhong) throws SQLException {
        return phimRepo.getPhimByTenPhong(tenPhong);
    }
}