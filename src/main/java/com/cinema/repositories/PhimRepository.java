package com.cinema.repositories;

import com.cinema.models.Phim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhimRepository extends BaseRepository<Phim> {
    public PhimRepository(Connection conn) {
        super(conn);
    }

    @Override
    public List<Phim> findAll() throws SQLException {
        List<Phim> list = new ArrayList<>();
        String sql = "SELECT * FROM Phim";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Phim(
                        rs.getInt("maPhim"),
                        rs.getString("tenPhim"),
                        rs.getInt("maTheLoai"),
                        rs.getInt("thoiLuong"),
                        rs.getDate("ngayKhoiChieu").toLocalDate(),
                        rs.getString("nuocSanXuat"),
                        rs.getString("dinhDang"),
                        rs.getString("moTa"),
                        rs.getString("daoDien")));
            }
        }
        return list;
    }

    @Override
    public Phim findById(int id) throws SQLException {
        String sql = "SELECT * FROM Phim WHERE maPhim=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Phim(
                        rs.getInt("maPhim"),
                        rs.getString("tenPhim"),
                        rs.getInt("maTheLoai"),
                        rs.getInt("thoiLuong"),
                        rs.getDate("ngayKhoiChieu").toLocalDate(),
                        rs.getString("nuocSanXuat"),
                        rs.getString("dinhDang"),
                        rs.getString("moTa"),
                        rs.getString("daoDien"));
            }
        }
        return null;
    }

    @Override
    public Phim save(Phim entity) throws SQLException {
        String sql = "INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getMaTheLoai());
            stmt.setInt(3, entity.getThoiLuong());
            stmt.setDate(4, Date.valueOf(entity.getNgayKhoiChieu()));
            stmt.setString(5, entity.getNuocSanXuat());
            stmt.setString(6, entity.getDinhDang());
            stmt.setString(7, entity.getMoTa());
            stmt.setString(8, entity.getDaoDien());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                entity.setMaPhim(rs.getInt(1));
            }
        }
        return entity;
    }

    @Override
    public Phim update(Phim entity) throws SQLException {
        String sql = "UPDATE Phim SET tenPhim=?, maTheLoai=?, thoiLuong=?, ngayKhoiChieu=?, nuocSanXuat=?, dinhDang=?, moTa=?, daoDien=? WHERE maPhim=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getMaTheLoai());
            stmt.setInt(3, entity.getThoiLuong());
            stmt.setDate(4, Date.valueOf(entity.getNgayKhoiChieu()));
            stmt.setString(5, entity.getNuocSanXuat());
            stmt.setString(6, entity.getDinhDang());
            stmt.setString(7, entity.getMoTa());
            stmt.setString(8, entity.getDaoDien());
            stmt.setInt(9, entity.getMaPhim());
            stmt.executeUpdate();
        }
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Phim WHERE maPhim=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}