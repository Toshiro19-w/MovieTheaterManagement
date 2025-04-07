package com.cinema.repositories;

import com.cinema.models.BaoCao;
import com.cinema.utils.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoRepository extends BaseRepository<BaoCao> {
    public BaoCaoRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

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

    @Override
    public List<BaoCao> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public BaoCao findById(int id) throws SQLException {
        return null;
    }

    @Override
    public BaoCao save(BaoCao entity) throws SQLException {
        return null;
    }

    @Override
    public BaoCao update(BaoCao entity) throws SQLException {
        return null;
    }

    @Override
    public void delete(int id) throws SQLException {

    }
}