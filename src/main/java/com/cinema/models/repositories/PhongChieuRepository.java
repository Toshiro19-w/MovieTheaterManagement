package com.cinema.models.repositories;

import com.cinema.models.PhongChieu;
import com.cinema.models.repositories.Interface.IPhongChieuRepository;
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
}