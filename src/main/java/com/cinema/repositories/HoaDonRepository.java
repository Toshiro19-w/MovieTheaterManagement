package com.cinema.repositories;

import com.cinema.models.HoaDon;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonRepository extends BaseRepository<HoaDon>{
    public HoaDonRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public List<HoaDon> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public HoaDon findById(int id) throws SQLException {
        return null;
    }

    @Override
    public HoaDon save(HoaDon entity) throws SQLException {
        return null;
    }

    @Override
    public HoaDon update(HoaDon entity) throws SQLException {
        return null;
    }

    @Override
    public void delete(int id) throws SQLException {

    }

    public int createHoaDon(Integer maNhanVien, Integer maKhachHang, BigDecimal tongTien) throws SQLException {
        String sql = "INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap, tongTien) VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, maNhanVien);
            stmt.setObject(2, maKhachHang);
            stmt.setBigDecimal(3, tongTien);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Trả về maHoaDon vừa tạo
                }
            }
        }
        throw new SQLException("Không thể tạo hóa đơn!");
    }

    public List<HoaDon> findByKhachHang(int maKhachHang) throws SQLException {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maKhachHang = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maKhachHang);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new HoaDon(
                            rs.getInt("maHoaDon"),
                            rs.getObject("maNhanVien") != null ? rs.getInt("maNhanVien") : null,
                            rs.getObject("maKhachHang") != null ? rs.getInt("maKhachHang") : null,
                            rs.getTimestamp("ngayLap").toLocalDateTime(),
                            rs.getBigDecimal("tongTien")
                    ));
                }
            }
        }
        return list;
    }
}
