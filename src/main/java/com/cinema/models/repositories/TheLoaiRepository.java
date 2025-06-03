package com.cinema.models.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.TheLoaiPhim;
import com.cinema.utils.DatabaseConnection;

public class TheLoaiRepository extends BaseRepository<TheLoaiPhim> {
    
    public TheLoaiRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<TheLoaiPhim> findAll() throws SQLException {
        List<TheLoaiPhim> list = new ArrayList<>();
        String sql = "SELECT * FROM TheLoaiPhim ORDER BY tenTheLoai";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TheLoaiPhim theLoai = new TheLoaiPhim();
                theLoai.setMaTheLoai(rs.getInt("maTheLoai"));
                theLoai.setTenTheLoai(rs.getString("tenTheLoai"));
                list.add(theLoai);
            }
        }
        return list;
    }

    @Override
    public TheLoaiPhim findById(int id) throws SQLException {
        String sql = "SELECT * FROM TheLoaiPhim WHERE maTheLoai = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                TheLoaiPhim theLoai = new TheLoaiPhim();
                theLoai.setMaTheLoai(rs.getInt("maTheLoai"));
                theLoai.setTenTheLoai(rs.getString("tenTheLoai"));
                return theLoai;
            }
        }
        return null;
    }

    @Override
    public TheLoaiPhim save(TheLoaiPhim entity) throws SQLException {
        String sql = "INSERT INTO TheLoaiPhim (tenTheLoai) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTenTheLoai());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                entity.setMaTheLoai(rs.getInt(1));
            }
        }
        return entity;
    }

    @Override
    public TheLoaiPhim update(TheLoaiPhim entity) throws SQLException {
        String sql = "UPDATE TheLoaiPhim SET tenTheLoai = ? WHERE maTheLoai = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getTenTheLoai());
            stmt.setInt(2, entity.getMaTheLoai());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể cập nhật thể loại. Không tìm thấy thể loại với mã: " + entity.getMaTheLoai());
            }
        }
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
        // Kiểm tra xem thể loại có đang được sử dụng không
        String checkSql = "SELECT COUNT(*) FROM PhimTheLoai WHERE maTheLoai = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Không thể xóa thể loại này vì đang được sử dụng bởi một hoặc nhiều phim.");
            }
        }
        
        // Nếu không được sử dụng, tiến hành xóa
        String sql = "DELETE FROM TheLoaiPhim WHERE maTheLoai = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public boolean isTheLoaiExists(String tenTheLoai, int excludeMaTheLoai) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TheLoaiPhim WHERE tenTheLoai = ? AND maTheLoai != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenTheLoai);
            stmt.setInt(2, excludeMaTheLoai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
}