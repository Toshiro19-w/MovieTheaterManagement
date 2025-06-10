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
    protected DatabaseConnection dbConnection;

    public BaoCaoRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
    }

    @Override
    public List<BaoCao> getBaoCaoDoanhThuTheoPhim(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        List<BaoCao> list = new ArrayList<>();
        String sql = "SELECT tenPhim, SoVeDaBan AS soVeBanRa, DoanhThuThucTe AS tongDoanhThu, DiemDanhGiaTrungBinh " +
                     "FROM ThongKeDoanhThuPhim " +
                     "WHERE EXISTS (" +
                     "    SELECT 1 FROM Ve v " +
                     "    JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                     "    WHERE sc.maPhim = ThongKeDoanhThuPhim.maPhim " +
                     "    AND v.trangThai = 'paid' " +
                     "    AND v.ngayDat BETWEEN ? AND ? " +
                     ") " +
                     "ORDER BY DoanhThuThucTe DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(tuNgay));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(denNgay));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BaoCao(
                            rs.getString("tenPhim"),
                            rs.getInt("soVeBanRa"),
                            rs.getDouble("tongDoanhThu"),
                            rs.getDouble("DiemDanhGiaTrungBinh")
                    ));
                }
            }
        }
        return list;
    }

    // Phương thức kiểm tra view có tồn tại
    public boolean isViewExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.views WHERE table_name = 'ThongKeDoanhThuPhim'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}