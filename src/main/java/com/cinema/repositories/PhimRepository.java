package com.cinema.repositories;

import com.cinema.models.Phim;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhimRepository extends BaseRepository<Phim> {
    public PhimRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Phim> findAll() throws SQLException {
        List<Phim> list = new ArrayList<>();
        String sql = "SELECT p.*, tl.tenTheLoai FROM Phim p JOIN TheLoaiPhim tl ON p.maTheLoai = tl.maTheLoai";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Phim phim = new Phim();
                phim.setMaPhim(rs.getInt("maPhim"));
                phim.setTenPhim(rs.getString("tenPhim"));
                phim.setMaTheLoai(rs.getInt("maTheLoai"));
                phim.setTenTheLoai(rs.getString("tenTheLoai"));
                phim.setThoiLuong(rs.getInt("thoiLuong"));
                phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null
                        ? rs.getDate("ngayKhoiChieu").toLocalDate()
                        : null);
                phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                phim.setDinhDang(rs.getString("dinhDang"));
                phim.setMoTa(rs.getString("moTa"));
                phim.setDaoDien(rs.getString("daoDien"));
                phim.setDuongDanPoster(rs.getString("duongDanPoster"));

                list.add(phim);
            }
        }
        return list;
    }

    public List<Phim> findAllDetail() throws SQLException {
        List<Phim> list = new ArrayList<>();
        String sql = "SELECT p.maPhim, p.tenPhim, p.maTheLoai, tl.tenTheLoai, p.thoiLuong, p.ngayKhoiChieu, " +
                "p.nuocSanXuat, p.dinhDang, p.moTa, p.daoDien, p.duongDanPoster, COUNT(sc.maSuatChieu) AS soSuatChieu " +
                "FROM Phim p " +
                "JOIN TheLoaiPhim tl ON p.maTheLoai = tl.maTheLoai " +
                "LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim " +
                "GROUP BY p.maPhim, p.tenPhim, p.maTheLoai, tl.tenTheLoai, p.thoiLuong, p.ngayKhoiChieu, " +
                "p.nuocSanXuat, p.dinhDang, p.moTa, p.daoDien " +
                "ORDER BY p.ngayKhoiChieu DESC, p.tenPhim";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Phim phim = new Phim();
                phim.setMaPhim(rs.getInt("maPhim"));
                phim.setTenPhim(rs.getString("tenPhim"));
                phim.setMaTheLoai(rs.getInt("maTheLoai"));
                phim.setTenTheLoai(rs.getString("tenTheLoai"));
                phim.setThoiLuong(rs.getInt("thoiLuong"));
                phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null
                        ? rs.getDate("ngayKhoiChieu").toLocalDate()
                        : null);
                phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                phim.setDinhDang(rs.getString("dinhDang"));
                phim.setMoTa(rs.getString("moTa"));
                phim.setDaoDien(rs.getString("daoDien"));
                phim.setDuongDanPoster(rs.getString("duongDanPoster"));

                list.add(phim);
            }
        }
        return list;
    }

    public List<Phim> searchPhim(String tenPhim, String tenTheLoai, String nuocSanXuat, String daoDien) throws SQLException {
        List<Phim> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.maPhim, p.tenPhim, p.maTheLoai, tl.tenTheLoai, p.thoiLuong, p.ngayKhoiChieu, " +
                        "p.nuocSanXuat, p.dinhDang, p.moTa, p.daoDien, p.duongDanPoster, COUNT(sc.maSuatChieu) AS soSuatChieu " +
                        "FROM Phim p " +
                        "JOIN TheLoaiPhim tl ON p.maTheLoai = tl.maTheLoai " +
                        "LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim " +
                        "WHERE 1=1"
        );

        List<String> params = new ArrayList<>();
        if (tenPhim != null && !tenPhim.trim().isEmpty()) {
            sql.append(" AND p.tenPhim LIKE ?");
            params.add("%" + tenPhim.trim() + "%");
        }
        if (tenTheLoai != null && !tenTheLoai.trim().isEmpty()) {
            sql.append(" AND tl.tenTheLoai LIKE ?");
            params.add("%" + tenTheLoai.trim() + "%");
        }
        if (nuocSanXuat != null && !nuocSanXuat.trim().isEmpty()) {
            sql.append(" AND p.nuocSanXuat LIKE ?");
            params.add("%" + nuocSanXuat.trim() + "%");
        }
        if (daoDien != null && !daoDien.trim().isEmpty()) {
            sql.append(" AND p.daoDien LIKE ?");
            params.add("%" + daoDien.trim() + "%");
        }

        sql.append(" GROUP BY p.maPhim, p.tenPhim, p.maTheLoai, tl.tenTheLoai, p.thoiLuong, p.ngayKhoiChieu, " +
                "p.nuocSanXuat, p.dinhDang, p.moTa, p.daoDien, p.duongDanPoster " +
                "ORDER BY p.ngayKhoiChieu DESC, p.tenPhim");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Phim phim = new Phim();
                phim.setMaPhim(rs.getInt("maPhim"));
                phim.setTenPhim(rs.getString("tenPhim"));
                phim.setMaTheLoai(rs.getInt("maTheLoai"));
                phim.setTenTheLoai(rs.getString("tenTheLoai"));
                phim.setThoiLuong(rs.getInt("thoiLuong"));
                phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null
                        ? rs.getDate("ngayKhoiChieu").toLocalDate()
                        : null);
                phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                phim.setDinhDang(rs.getString("dinhDang"));
                phim.setMoTa(rs.getString("moTa"));
                phim.setDaoDien(rs.getString("daoDien"));
                phim.setDuongDanPoster(rs.getString("duongDanPoster"));
                list.add(phim);
            }
        }
        return list;
    }

    @Override
    public Phim save(Phim entity) throws SQLException {
        String sql = "INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien, duongDanPoster) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getMaTheLoai());
            stmt.setInt(3, entity.getThoiLuong());
            stmt.setDate(4, entity.getNgayKhoiChieu() != null ? Date.valueOf(entity.getNgayKhoiChieu()) : null);
            stmt.setString(5, entity.getNuocSanXuat());
            stmt.setString(6, entity.getDinhDang());
            stmt.setString(7, entity.getMoTa());
            stmt.setString(8, entity.getDaoDien());
            stmt.setString(9, entity.getDuongDanPoster());
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
        String sql = "UPDATE Phim SET tenPhim=?, maTheLoai=?, thoiLuong=?, ngayKhoiChieu=?, nuocSanXuat=?, dinhDang=?, moTa=?, daoDien=?, duongDanPoster=? WHERE maPhim=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getMaTheLoai());
            stmt.setInt(3, entity.getThoiLuong());
            stmt.setDate(4, entity.getNgayKhoiChieu() != null ? Date.valueOf(entity.getNgayKhoiChieu()) : null);
            stmt.setString(5, entity.getNuocSanXuat());
            stmt.setString(6, entity.getDinhDang());
            stmt.setString(7, entity.getMoTa());
            stmt.setString(8, entity.getDaoDien());
            stmt.setString(9, entity.getDuongDanPoster());
            stmt.setInt(10, entity.getMaPhim());
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