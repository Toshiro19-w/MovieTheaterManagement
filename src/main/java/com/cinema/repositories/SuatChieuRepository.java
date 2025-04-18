package com.cinema.repositories;

import com.cinema.models.SuatChieu;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SuatChieuRepository extends BaseRepository<SuatChieu> {
    public SuatChieuRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<SuatChieu> findAll() {
        List<SuatChieu> list = new ArrayList<>();
        String sql = "SELECT * FROM SuatChieu";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new SuatChieu(
                        rs.getInt("maSuatChieu"),
                        rs.getInt("maPhim"),
                        rs.getInt("maPhong"),
                        rs.getTimestamp("ngayGioChieu").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn tất cả suất chiếu: " + e.getMessage(), e);
        }
        return list;
    }

    public List<SuatChieu> findAllDetail() {
        List<SuatChieu> list = new ArrayList<>();
        String sql = "SELECT sc.maSuatChieu, p.tenPhim, " +
                "pc.tenPhong, sc.ngayGioChieu, p.thoiLuong, p.dinhDang " +
                "FROM SuatChieu sc " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new SuatChieu(
                        rs.getInt("maSuatChieu"),
                        rs.getString("tenPhim"),
                        rs.getString("tenPhong"),
                        rs.getTimestamp("ngayGioChieu").toLocalDateTime(),
                        rs.getInt("thoiLuong"),
                        rs.getString("dinhDang")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn chi tiết tất cả suất chiếu: " + e.getMessage(), e);
        }
        return list;
    }

    public List<SuatChieu> searchSuatChieuByNgay(LocalDateTime ngayGioChieu) throws SQLException {
        List<SuatChieu> suatChieuList = new ArrayList<>();
        String sql = "SELECT sc.maSuatChieu, sc.maPhim, p.tenPhim, " +
                "pc.tenPhong, sc.ngayGioChieu, p.thoiLuong, p.dinhDang " +
                "FROM SuatChieu sc " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong " +
                "WHERE sc.ngayGioChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(ngayGioChieu));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suatChieuList.add(new SuatChieu(
                        rs.getInt("maSuatChieu"),
                        rs.getString("tenPhim"),
                        rs.getString("tenPhong"),
                        rs.getTimestamp("ngayGioChieu").toLocalDateTime(),
                        rs.getInt("thoiLuong"),
                        rs.getString("dinhDang")
                ));
            }
        }
        return suatChieuList;
    }


    public List<SuatChieu> findByMaPhim(int maPhim) throws SQLException {
        List<SuatChieu> suatChieuList = new ArrayList<>();
        String sql = "SELECT sc.maSuatChieu, p.tenPhim, p.thoiLuong AS thoiLuongPhim, " +
                "p.dinhDang AS dinhDangPhim, pc.tenPhong, sc.maPhong, sc.ngayGioChieu " +
                "FROM SuatChieu sc " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong " +
                "WHERE sc.maPhim = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suatChieuList.add(new SuatChieu(
                        rs.getInt("maSuatChieu"),
                        rs.getString("tenPhim"),
                        rs.getInt("thoiLuongPhim"),
                        rs.getString("dinhDangPhim"),
                        rs.getString("tenPhong"),
                        rs.getInt("maPhong"),
                        rs.getTimestamp("ngayGioChieu") != null ? rs.getTimestamp("ngayGioChieu").toLocalDateTime() : null
                ));
            }
        }
        return suatChieuList;
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
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi thêm suất chiếu: " + e.getMessage(), e);
        }
    }

    @Override
    public SuatChieu update(SuatChieu entity) throws SQLException {
        String sql = "UPDATE SuatChieu SET maPhim=?, maPhong=?, ngayGioChieu=? WHERE maSuatChieu=?";
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
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi cập nhật suất chiếu: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM SuatChieu WHERE maSuatChieu=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Xóa suất chiếu thất bại, không tìm thấy suất chiếu với ID: " + id);
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi xóa suất chiếu: " + e.getMessage(), e);
        }
    }
}