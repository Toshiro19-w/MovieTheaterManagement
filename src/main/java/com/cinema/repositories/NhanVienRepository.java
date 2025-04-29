package com.cinema.repositories;

import com.cinema.models.LoaiNguoiDung;
import com.cinema.models.NhanVien;
import com.cinema.models.VaiTro;
import com.cinema.utils.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhanVienRepository extends BaseRepository<NhanVien> {
    public NhanVienRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    public List<NhanVien> findAll() throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email, nd.loaiNguoiDung, " +
                "nv.luong, nv.vaiTro " +
                "FROM NguoiDung nd JOIN NhanVien nv ON nd.maNguoiDung = nv.maNguoiDung";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new NhanVien(
                        rs.getInt("maNguoiDung"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        rs.getString("email"),
                        LoaiNguoiDung.fromString(rs.getString("loaiNguoiDung")),
                        rs.getBigDecimal("luong"),
                        VaiTro.fromString(rs.getString("vaiTro"))
                ));
            }
        }
        return list;
    }

    @Override
    public NhanVien save(NhanVien entity) throws SQLException {
        String sqlNguoiDung = "INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlNguoiDung, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getHoTen());
            stmt.setString(2, entity.getSoDienThoai());
            stmt.setString(3, entity.getEmail());
            stmt.setString(4, "NhanVien");
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                entity.setMaNguoiDung(rs.getInt(1));
            }
        }

        String sqlNhanVien = "INSERT INTO NhanVien (maNguoiDung, luong, vaiTro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlNhanVien)) {
            stmt.setInt(1, entity.getMaNguoiDung());
            stmt.setBigDecimal(2, entity.getLuong());
            stmt.setString(3, entity.getVaiTro().toString());
            stmt.executeUpdate();
        }

        return entity;
    }

    @Override
    public NhanVien update(NhanVien entity) throws SQLException {
        String sqlNguoiDung = "UPDATE NguoiDung SET hoTen = ?, soDienThoai = ?, email = ? WHERE maNguoiDung = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlNguoiDung)) {
            stmt.setString(1, entity.getHoTen());
            stmt.setString(2, entity.getSoDienThoai());
            stmt.setString(3, entity.getEmail());
            stmt.setInt(4, entity.getMaNguoiDung());
            stmt.executeUpdate();
        }

        String sqlNhanVien = "UPDATE NhanVien SET luong = ?, vaiTro = ? WHERE maNguoiDung = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlNhanVien)) {
            stmt.setBigDecimal(1, entity.getLuong());
            stmt.setString(2, entity.getVaiTro().toString());
            stmt.setInt(3, entity.getMaNguoiDung());
            stmt.executeUpdate();
        }

        return entity;
    }

    @Override
    public void delete(int maNguoiDung) throws SQLException {
        String sql = "DELETE FROM NguoiDung WHERE maNguoiDung = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maNguoiDung);
            stmt.executeUpdate();
        }
    }

    public List<NhanVien> searchNhanVien(String hoTen) throws SQLException {
        List<NhanVien> nhanVienList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email, nd.loaiNguoiDung, " +
                        "nv.luong, nv.vaiTro " +
                        "FROM NguoiDung nd " +
                        "JOIN NhanVien nv ON nd.maNguoiDung = nv.maNguoiDung " +
                        "WHERE nd.loaiNguoiDung = 'NhanVien'"
        );

        List<Object> params = new ArrayList<>();
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            sql.append(" AND nd.hoTen LIKE ?");
            params.add("%" + hoTen.trim() + "%");
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                nhanVienList.add(new NhanVien(
                        rs.getInt("maNguoiDung"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        rs.getString("email"),
                        LoaiNguoiDung.fromString(rs.getString("loaiNguoiDung")),
                        rs.getBigDecimal("luong"),
                        VaiTro.fromString(rs.getString("vaiTro"))
                ));
            }
        }
        return nhanVienList;
    }
}