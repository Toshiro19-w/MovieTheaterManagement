package com.cinema.repositories;

import com.cinema.models.PhongChieu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongChieuRepository extends BaseRepository<PhongChieu> {
    public PhongChieuRepository(Connection conn) {
        super(conn);
    }

    @Override
    public List<PhongChieu> findAll() throws SQLException {
        List<PhongChieu> list = new ArrayList<>();
        String sql = "SELECT * FROM PhongChieu";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new PhongChieu(rs.getInt("maPhong"), rs.getInt("soLuongGhe"), rs.getString("loaiPhong")));
            }
        }
        return list;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM PhongChieu WHERE maPhong=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public PhongChieu findById(int id) throws SQLException {
        String sql = "SELECT * FROM PhongChieu WHERE maPhong=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PhongChieu(rs.getInt("maPhong"), rs.getInt("soLuongGhe"), rs.getString("loaiPhong"));
            }
        }
        return null;
    }

    @Override
    public PhongChieu save(PhongChieu entity) throws SQLException {
        String sql = "INSERT INTO PhongChieu (maPhong, soLuongGhe, loaiPhong) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.getMaPhong());
            stmt.setInt(2, entity.getSoLuongGhe());
            stmt.setString(3, entity.getLoaiPhong());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                entity.setMaPhong(rs.getInt(1));
            }
        }
        return entity;
    }

    @Override
    public PhongChieu update(PhongChieu entity) throws SQLException {
        String sql = "UPDATE PhongChieu SET soLuongGhe=?, loaiPhong=? WHERE maPhong=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getSoLuongGhe());
            stmt.setString(2, entity.getLoaiPhong());
            stmt.setInt(3, entity.getMaPhong());
            stmt.executeUpdate();
        }
        return entity;
    }
}
