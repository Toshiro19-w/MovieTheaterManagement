package com.cinema.repositories;

import com.cinema.models.PhongChieu;
import com.cinema.repositories.Interface.IPhongChieuRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongChieuRepository implements IPhongChieuRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public PhongChieuRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
    }

    @Override
    public List<PhongChieu> findAll() throws SQLException {
        List<PhongChieu> list = new ArrayList<>();
        String sql = "SELECT * FROM PhongChieu";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new PhongChieu(
                        rs.getInt("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getInt("soLuongGhe"),
                        rs.getString("loaiPhong")
                ));
            }
        }
        return list;
    }

    @Override
    public List<PhongChieu> findByTenPhong(String tenPhong) throws SQLException {
        List<PhongChieu> list = new ArrayList<>();
        String sql = "SELECT * FROM PhongChieu WHERE tenPhong LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + tenPhong + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new PhongChieu(
                        rs.getInt("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getInt("soLuongGhe"),
                        rs.getString("loaiPhong")
                ));
            }
        }
        return list;
    }

    @Override
    public PhongChieu save(PhongChieu entity) throws SQLException {
        String sql = "INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.getSoLuongGhe());
            stmt.setString(2, entity.getLoaiPhong());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Thêm phòng chiếu thất bại, không có hàng nào được tạo.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setMaPhong(generatedKeys.getInt(1));
                    return entity;
                } else {
                    throw new SQLException("Thêm phòng chiếu thất bại, không có ID nào được trả về.");
                }
            }
        }
    }
}