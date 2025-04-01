package com.cinema.repositories;

import com.cinema.models.Ve;
import com.cinema.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeRepository implements IVeRepository {
    private final Connection conn;

    public VeRepository() {
        this.conn = DatabaseConnection.getConnection();
    }

    @Override
    public List<Ve> getAllVeBasic() {
        List<Ve> list = new ArrayList<>();
        String sql = "SELECT v.*, sc.ngayGioChieu, p.tenPhim, pc.loaiPhong " +
                "FROM Ve v " +
                "JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("maVe"));
                ve.setMaSuatChieu(rs.getInt("maSuatChieu"));
                ve.setMaKhachHang(rs.getObject("maKhachHang", Integer.class));
                ve.setMaHoaDon(rs.getObject("maHoaDon", Integer.class));
                ve.setSoGhe(rs.getString("soGhe"));
                ve.setGiaVe(rs.getDouble("giaVe"));
                ve.setTrangThai(rs.getString("trangThai"));
                ve.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                ve.setTenPhim(rs.getString("tenPhim"));
                ve.setLoaiPhong(rs.getString("loaiPhong"));

                list.add(ve);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Ve> getAllVeCuaToi(String email) {
        List<Ve> list = new ArrayList<>();
        String sql = "SELECT v.*, p.tenPhim, k.hoTen, sc.ngayGioChieu, pc.loaiPhong " +
                "FROM Ve v " +
                "JOIN KhachHang k ON v.maKhachHang = k.maKhachHang " +
                "JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong " +
                "WHERE k.email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ve ve = new Ve();
                    ve.setMaVe(rs.getInt("maVe"));
                    ve.setTenPhim(rs.getString("tenPhim"));
                    ve.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                    ve.setHoTenKhachHang(rs.getString("hoTen"));
                    ve.setSoGhe(rs.getString("soGhe"));
                    ve.setGiaVe(rs.getDouble("giaVe"));
                    ve.setLoaiPhong(rs.getString("loaiPhong"));
                    ve.setTrangThai(rs.getString("trangThai"));

                    list.add(ve);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean suaVe(Ve ve) {
        String sql = "UPDATE Ve SET maSuatChieu=?, maKhachHang=?, maHoaDon=?, soGhe=?, giaVe=?, trangThai=? WHERE maVe=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setObject(2, ve.getMaKhachHang());
            stmt.setObject(3, ve.getMaHoaDon());
            stmt.setString(4, ve.getSoGhe());
            stmt.setDouble(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai());
            stmt.setInt(7, ve.getMaVe());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean xoaVe(int maVe) {
        String sql = "DELETE FROM Ve WHERE maVe=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean themVe(Ve ve) {
        String sql = "INSERT INTO Ve (maSuatChieu, maKhachHang, maHoaDon, soGhe, giaVe, trangThai) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setObject(2, ve.getMaKhachHang());
            stmt.setObject(3, ve.getMaHoaDon());
            stmt.setString(4, ve.getSoGhe());
            stmt.setDouble(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ve.setMaVe(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}