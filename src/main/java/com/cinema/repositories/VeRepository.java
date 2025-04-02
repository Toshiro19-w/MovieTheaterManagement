package com.cinema.repositories;

import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VeRepository implements IVeRepository {
    private final Connection conn;
    private LocalDate ngayDat;

    public VeRepository() {
        this.conn = DatabaseConnection.getConnection();
    }

    @Override
    public List<Ve> findAll() throws SQLException {
        String sql = "SELECT * FROM Ve";
        List<Ve> result = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                result.add(mapResultSetToVe(resultSet));
            }

            return result;
        }
    }

    @Override
    public Ve findByMaVe(int maVe) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToVe(resultSet);
            }
            return null;
        }
    }

    @Override
    public List<Ve> findByMaSuatChieu(int maSuatChieu) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return (List<Ve>) mapResultSetToVe(resultSet);
            }
            return null;
        }
    }

    @Override
    public List<Ve> findByMaKhachHang(Integer maKhachHang, int page, int pageSize) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE maKhachHang = ? LIMIT ? OFFSET ?";
        List<Ve> result = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maKhachHang);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                result.add(mapResultSetToVe(resultSet));
            }

            return result;
        }
    }

    @Override
    public List<Ve> findByMaHoaDon(Integer maHoaDon) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE maHoaDon = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHoaDon);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return (List<Ve>) mapResultSetToVe(resultSet);
            }
            return null;
        }
    }

    @Override
    public List<Ve> findByTrangThai(TrangThaiVe trangThai, int page, int pageSize) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE trangThai = ? LIMIT ? OFFSET ?";
        List<Ve> result = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trangThai.getValue());
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                result.add(mapResultSetToVe(resultSet));
            }

            return result;
        }
    }

    @Override
    public List<Ve> findByNgayDat(LocalDate ngayDat) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE ngayDat = ?";
        List<Ve> result = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(ngayDat));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                result.add(mapResultSetToVe(resultSet));
            }

            return result;
        }
    }

    @Override
    public Ve findVeChiTietByMaVe(int maVe) throws SQLException {
        String sql = "SELECT v.*, p.tenPhim, sc.ngayGioChieu, kh.hoTenKhachHang, pc.loaiPhong " +
                "FROM Ve v " +
                "JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "LEFT JOIN KhachHang kh ON v.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN PhongChieu pc ON sc.maPhongChieu = pc.maPhongChieu " +
                "WHERE v.maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Ve ve = mapResultSetToVe(resultSet);
                ve.setTenPhim(resultSet.getString("tenPhim"));
                ve.setNgayGioChieu(resultSet.getTimestamp("ngayGioChieu").toLocalDateTime().toLocalDate());
                ve.setHoTenKhachHang(resultSet.getString("hoTenKhachHang"));
                ve.setLoaiPhong(resultSet.getString("loaiPhong"));
                return ve;
            }
            return null;
        }
    }

    @Override
    public Ve save(Ve ve) throws SQLException {
        String sql = "INSERT INTO Ve (maSuatChieu, maKhachHang, maHoaDon, soGhe, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setObject(2, ve.getMaKhachHang() == null ? null : ve.getMaKhachHang());
            stmt.setObject(3, ve.getMaHoaDon() == null ? null : ve.getMaHoaDon());
            stmt.setString(4, ve.getSoGhe());
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat());
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                ve.setMaVe(generatedKeys.getInt(1));
            }
            return ve;
        }
    }

    @Override
    public Ve update(Ve ve) throws SQLException {
        String sql = "UPDATE Ve SET maSuatChieu=?, maKhachHang=?, maHoaDon=?, soGhe=?, giaVe=?, trangThai=?, ngayDat=? WHERE maVe=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setObject(2, ve.getMaKhachHang() == null ? null : ve.getMaKhachHang());
            stmt.setObject(3, ve.getMaHoaDon() == null ? null : ve.getMaHoaDon());
            stmt.setString(4, ve.getSoGhe());
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat());
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                ve.setMaVe(generatedKeys.getInt(1));
            }
            return ve;
        }
    }

    @Override
    public void delete(int maVe) throws SQLException {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            stmt.executeUpdate();
        }
    }

    private Ve mapResultSetToVe(ResultSet resultSet) throws SQLException {
        Ve ve = new Ve();
        ve.setMaVe(resultSet.getInt("maVe"));
        ve.setMaSuatChieu(resultSet.getInt("maSuatChieu"));
        ve.setMaKhachHang(resultSet.getObject("maKhachHang", Integer.class));
        ve.setMaHoaDon(resultSet.getObject("maHoaDon", Integer.class));
        ve.setSoGhe(resultSet.getString("soGhe"));
        ve.setGiaVe(resultSet.getBigDecimal("giaVe"));
        ve.setTrangThai(TrangThaiVe.fromString(resultSet.getString("trangThai")));
        ve.setNgayDat(resultSet.getDate("ngayDat") != null ? resultSet.getDate("ngayDat").toLocalDate() : null);
        return ve;
    }
}