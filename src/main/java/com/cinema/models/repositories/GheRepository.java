package com.cinema.models.repositories;

import com.cinema.models.Ghe;
import com.cinema.models.repositories.Interface.IGheRepository;
import com.cinema.utils.DatabaseConnection;
import com.mysql.cj.jdbc.CallableStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GheRepository implements IGheRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public GheRepository(DatabaseConnection dbConnection) {
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
    public List<Ghe> findGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException {
        List<Ghe> availableSeats = new ArrayList<>();
        String sql = "{CALL CheckAvailableSeats(?)}";
        
        try (CallableStatement stmt = (CallableStatement) conn.prepareCall(sql)) {
            stmt.setInt(1, maSuatChieu);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Ghe ghe = new Ghe();
                ghe.setMaGhe(rs.getInt("maGhe"));
                ghe.setMaPhong(rs.getInt("maPhong"));
                ghe.setSoGhe(rs.getString("soGhe"));
                ghe.setLoaiGhe(rs.getString("loaiGhe"));
                availableSeats.add(ghe);
            }
        }
        
        return availableSeats;
    }

    @Override
    public List<Ghe> findAllGheByPhong(int maPhong) throws SQLException {
        List<Ghe> gheList = new ArrayList<>();
        String sql = "SELECT maPhong, soGhe FROM Ghe WHERE maPhong = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ghe ghe = new Ghe();
                    ghe.setMaPhong(rs.getInt("maPhong"));
                    ghe.setSoGhe(rs.getString("soGhe"));
                    gheList.add(ghe);
                }
            }
        }
        return gheList;
    }
}