package com.cinema.models.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.SuatChieu;
import com.cinema.utils.DatabaseConnection;

public class SuatChieuRepository extends BaseRepository<SuatChieu> {
    public SuatChieuRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<SuatChieu> findAll() {
        List<SuatChieu> list = new ArrayList<>();
        String sql = "SELECT sc.maSuatChieu, sc.maPhim, p.tenPhim, sc.maPhong, pc.tenPhong, " +
                "sc.ngayGioChieu, p.thoiLuong, p.kieuPhim " +
                "FROM SuatChieu sc " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new SuatChieu(
                        rs.getInt("maSuatChieu"),
                        rs.getInt("maPhim"),
                        rs.getString("tenPhim"),
                        rs.getInt("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getTimestamp("ngayGioChieu").toLocalDateTime(),
                        rs.getInt("thoiLuong"),
                        rs.getString("kieuPhim")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn chi tiết suất chiếu: " + e.getMessage(), e);
        }
        return list;
    }

    public List<SuatChieu> findByMaPhim(int maPhim) throws SQLException {
        List<SuatChieu> list = new ArrayList<>();
        String sql = "SELECT sc.maSuatChieu, sc.maPhim, p.tenPhim, sc.maPhong, pc.tenPhong, " +
                "sc.ngayGioChieu, p.thoiLuong, p.kieuPhim " +
                "FROM SuatChieu sc " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong " +
                "WHERE sc.maPhim = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new SuatChieu(
                        rs.getInt("maSuatChieu"),
                        rs.getInt("maPhim"),
                        rs.getString("tenPhim"),
                        rs.getInt("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getTimestamp("ngayGioChieu").toLocalDateTime(),
                        rs.getInt("thoiLuong"),
                        rs.getString("kieuPhim")
                ));
            }
        }
        return list;
    }

    @Override
    public SuatChieu save(SuatChieu entity) throws SQLException {
        String sql = "INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.getMaPhim());
            stmt.setInt(2, entity.getMaPhong());
            stmt.setTimestamp(3, Timestamp.valueOf(entity.getNgayGioChieu()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Thêm suất chiếu thất bại, không có hàng nào được tạo.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setMaSuatChieu(generatedKeys.getInt(1));
                    return entity;
                } else {
                    throw new SQLException("Thêm suất chiếu thất bại, không có ID nào được trả về.");
                }
            }
        }
    }

    @Override
    public SuatChieu update(SuatChieu entity) throws SQLException {
        String sql = "UPDATE SuatChieu SET maPhim = ?, maPhong = ?, ngayGioChieu = ? WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getMaPhim());
            stmt.setInt(2, entity.getMaPhong());
            stmt.setTimestamp(3, Timestamp.valueOf(entity.getNgayGioChieu()));
            stmt.setInt(4, entity.getMaSuatChieu());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return entity;
            } else {
                throw new SQLException("Cập nhật suất chiếu thất bại, không tìm thấy suất chiếu với ID: " + entity.getMaSuatChieu());
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM SuatChieu WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Xóa suất chiếu thất bại, không tìm thấy suất chiếu với ID: " + id);
            }
        }
    }

    public List<String> getThoiGianChieuByPhongVaPhim(String tenPhong, String tenPhim) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT sc.ngayGioChieu FROM SuatChieu sc " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong " +
                "WHERE pc.tenPhong = ? AND p.tenPhim = ? ORDER BY sc.ngayGioChieu";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhong);
            stmt.setString(2, tenPhim);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("ngayGioChieu");
                if (ts != null) {
                    java.time.LocalDateTime ldt = ts.toLocalDateTime();
                    String formatted = ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    list.add(formatted);
                }
            }
        }
        return list;
    }

    public boolean hasShowtimeBefore(int maPhim, java.time.LocalDate newReleaseDate) throws SQLException {
        String sql = """
            SELECT 1 FROM SuatChieu
            WHERE maPhim = ? AND DATE(ngayGioChieu) < ?
            LIMIT 1
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            stmt.setDate(2, java.sql.Date.valueOf(newReleaseDate));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}