package com.cinema.models.repositories;

import com.cinema.models.BaoCao;
import com.cinema.models.repositories.Interface.IBaoCaoRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoRepository implements IBaoCaoRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public BaoCaoRepository(DatabaseConnection dbConnection) {
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
    public List<BaoCao> getBaoCaoDoanhThuTheoPhim(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        List<BaoCao> list = new ArrayList<>();
        String sql = "SELECT p.tenPhim, COUNT(v.maVe) AS soVeBanRa, SUM(v.giaVe) AS tongDoanhThu " +
                "FROM Phim p " +
                "LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim " +
                "LEFT JOIN Ve v ON sc.maSuatChieu = v.maSuatChieu " +
                "WHERE v.trangThai = 'paid' " +
                "AND v.ngayDat BETWEEN ? AND ? " +
                "GROUP BY p.maPhim, p.tenPhim " +
                "ORDER BY tongDoanhThu DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(tuNgay));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(denNgay));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new BaoCao(
                        rs.getString("tenPhim"),
                        rs.getInt("soVeBanRa"),
                        rs.getDouble("tongDoanhThu")
                ));
            }
        }
        return list;
    }
}