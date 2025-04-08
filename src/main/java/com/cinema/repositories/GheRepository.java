package com.cinema.repositories;

import com.cinema.models.Ghe;
import com.cinema.repositories.Interface.IGheRepository;
import com.cinema.utils.DatabaseConnection;

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

    public List<Ghe> findGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException {
        List<Ghe> list = new ArrayList<>();
        String sql = "SELECT g.maPhong, g.soGhe " +
                "FROM Ghe g " +
                "WHERE g.maPhong = ? " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM Ve v " +
                "WHERE v.maSuatChieu = ? AND v.maPhong = g.maPhong AND v.soGhe = g.soGhe" +
                ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            stmt.setInt(2, maSuatChieu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Ghe(
                        rs.getInt("maPhong"),
                        rs.getString("soGhe")
                ));
            }
        }
        return list;
    }
}