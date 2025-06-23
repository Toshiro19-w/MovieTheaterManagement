package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TheLoaiPhim theLoai = new TheLoaiPhim();
                theLoai.setMaTheLoai(rs.getInt("maTheLoai"));
                theLoai.setTenTheLoai(rs.getString("tenTheLoai"));
                list.add(theLoai);
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tải danh sách thể loại: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public TheLoaiPhim findById(int id) throws SQLException {
        String sql = "SELECT * FROM TheLoaiPhim WHERE maTheLoai = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TheLoaiPhim theLoai = new TheLoaiPhim();
                    theLoai.setMaTheLoai(rs.getInt("maTheLoai"));
                    theLoai.setTenTheLoai(rs.getString("tenTheLoai"));
                    return theLoai;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tìm thể loại với mã " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public TheLoaiPhim save(TheLoaiPhim entity) throws SQLException {
        if (entity.getTenTheLoai() == null || entity.getTenTheLoai().trim().isEmpty()) {
            throw new SQLException("Tên thể loại không được để trống");
        }
        String sql = "INSERT INTO TheLoaiPhim (tenTheLoai) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTenTheLoai().trim());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể thêm thể loại");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setMaTheLoai(rs.getInt(1));
                } else {
                    throw new SQLException("Không thể lấy mã thể loại vừa thêm");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi thêm thể loại: " + e.getMessage(), e);
        }
        return entity;
    }

    @Override
    public TheLoaiPhim update(TheLoaiPhim entity) throws SQLException {
        if (entity.getTenTheLoai() == null || entity.getTenTheLoai().trim().isEmpty()) {
            throw new SQLException("Tên thể loại không được để trống");
        }
        String sql = "UPDATE TheLoaiPhim SET tenTheLoai = ? WHERE maTheLoai = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getTenTheLoai().trim());
            stmt.setInt(2, entity.getMaTheLoai());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể cập nhật thể loại. Không tìm thấy thể loại với mã: " + entity.getMaTheLoai());
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi cập nhật thể loại: " + e.getMessage(), e);
        }
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM PhimTheLoai WHERE maTheLoai = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Không thể xóa thể loại này vì đang được sử dụng bởi một hoặc nhiều phim.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi kiểm tra ràng buộc thể loại: " + e.getMessage(), e);
        }

        String sql = "DELETE FROM TheLoaiPhim WHERE maTheLoai = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể xóa thể loại. Không tìm thấy thể loại với mã: " + id);
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi xóa thể loại: " + e.getMessage(), e);
        }
    }
    
    public boolean isTheLoaiExists(String tenTheLoai, int excludeMaTheLoai) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TheLoaiPhim WHERE tenTheLoai = ? AND maTheLoai != ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenTheLoai.trim());
            stmt.setInt(2, excludeMaTheLoai);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi kiểm tra sự tồn tại của thể loại: " + e.getMessage(), e);
        }
        return false;
    }
    
    public Map<Integer, String> getAllTheLoaiMap() throws SQLException {
        Map<Integer, String> theLoaiMap = new HashMap<>();
        String sql = "SELECT maTheLoai, tenTheLoai FROM TheLoaiPhim ORDER BY tenTheLoai";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                theLoaiMap.put(rs.getInt("maTheLoai"), rs.getString("tenTheLoai"));
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tải danh sách thể loại dưới dạng map: " + e.getMessage(), e);
        }
        
        return theLoaiMap.isEmpty() ? new HashMap<>() : theLoaiMap;
    }
    
    public List<String> getAllTheLoai() throws SQLException {
        List<String> theLoaiList = new ArrayList<>();
        String sql = "SELECT tenTheLoai FROM TheLoaiPhim ORDER BY tenTheLoai";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                theLoaiList.add(rs.getString("tenTheLoai"));
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tải danh sách tên thể loại: " + e.getMessage(), e);
        }
        
        return theLoaiList;
    }
    
    public int getMaTheLoaiByTen(String tenTheLoai) throws SQLException {
        String sql = "SELECT maTheLoai FROM TheLoaiPhim WHERE tenTheLoai = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenTheLoai.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maTheLoai");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy mã thể loại theo tên: " + e.getMessage(), e);
        }
        return -1; // Trả về -1 nếu không tìm thấy
    }
}