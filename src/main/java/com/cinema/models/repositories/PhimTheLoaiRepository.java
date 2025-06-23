package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cinema.utils.DatabaseConnection;

/**
 * Lớp repository để quản lý mối quan hệ giữa Phim và TheLoaiPhim
 */
public class PhimTheLoaiRepository {
    private final DatabaseConnection dbConnection;


    
    public PhimTheLoaiRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
    }
    
    /**
     * Thêm một thể loại cho phim
     * 
     * @param maPhim Mã phim
     * @param maTheLoai Mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public void addTheLoaiForPhim(int maPhim, int maTheLoai) throws SQLException {
        String sql = "INSERT INTO PhimTheLoai (maPhim, maTheLoai) VALUES (?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            stmt.setInt(2, maTheLoai);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Thêm nhiều thể loại cho phim
     * 
     * @param maPhim Mã phim
     * @param maTheLoaiList Danh sách mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public void addTheLoaisForPhim(int maPhim, List<Integer> maTheLoaiList) throws SQLException {
        String sql = "INSERT INTO PhimTheLoai (maPhim, maTheLoai) VALUES (?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer maTheLoai : maTheLoaiList) {
                stmt.setInt(1, maPhim);
                stmt.setInt(2, maTheLoai);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    /**
     * Xóa tất cả thể loại của một phim
     * 
     * @param maPhim Mã phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public void deleteAllTheLoaiOfPhim(int maPhim) throws SQLException {
        String sql = "DELETE FROM PhimTheLoai WHERE maPhim = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Lấy danh sách mã thể loại của một phim
     * 
     * @param maPhim Mã phim
     * @return Danh sách mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<Integer> getTheLoaiIdsByPhimId(int maPhim) throws SQLException {
        List<Integer> theLoaiIds = new ArrayList<>();
        String sql = "SELECT maTheLoai FROM PhimTheLoai WHERE maPhim = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    theLoaiIds.add(rs.getInt("maTheLoai"));
                }
            }
        }
        return theLoaiIds;
    }
    
    /**
     * Lấy danh sách tên thể loại của một phim
     * 
     * @param maPhim Mã phim
     * @return Danh sách tên thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<String> getTheLoaiNamesByPhimId(int maPhim) throws SQLException {
        List<String> theLoaiNames = new ArrayList<>();
        String sql = "SELECT tl.tenTheLoai FROM PhimTheLoai pt " +
                     "JOIN TheLoaiPhim tl ON pt.maTheLoai = tl.maTheLoai " +
                     "WHERE pt.maPhim = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    theLoaiNames.add(rs.getString("tenTheLoai"));
                }
            }
        }
        return theLoaiNames;
    }
    
    /**
     * Lấy danh sách tên thể loại của một phim dưới dạng chuỗi phân cách bởi dấu phẩy
     * 
     * @param maPhim Mã phim
     * @return Chuỗi tên thể loại phân cách bởi dấu phẩy
     * @throws SQLException Nếu có lỗi SQL
     */
    public String getTheLoaiNamesStringByPhimId(int maPhim) throws SQLException {
        List<String> theLoaiNames = getTheLoaiNamesByPhimId(maPhim);
        return String.join(", ", theLoaiNames);
    }
    
    /**
     * Cập nhật thể loại cho phim
     * 
     * @param maPhim Mã phim
     * @param maTheLoaiList Danh sách mã thể loại mới
     * @throws SQLException Nếu có lỗi SQL
     */
    public void updateTheLoaisForPhim(int maPhim, List<Integer> maTheLoaiList) throws SQLException {
        // Xóa tất cả thể loại cũ
        deleteAllTheLoaiOfPhim(maPhim);
        
        // Thêm thể loại mới
        if (maTheLoaiList != null && !maTheLoaiList.isEmpty()) {
            addTheLoaisForPhim(maPhim, maTheLoaiList);
        }
    }
    public void addTheLoai(String tenTheLoai) throws SQLException {
        String sql = "INSERT INTO theloai (ten_the_loai) VALUES (?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTheLoai);
            ps.executeUpdate();
        }
    }

    public void updateTheLoai(int maTheLoai, String tenTheLoai) throws SQLException {
        String sql = "UPDATE theloai SET ten_the_loai = ? WHERE ma_the_loai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTheLoai);
            ps.setInt(2, maTheLoai);
            ps.executeUpdate();
        }
    }

    public void deleteTheLoai(int maTheLoai) throws SQLException {
        String sql = "DELETE FROM theloai WHERE ma_the_loai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTheLoai);
            ps.executeUpdate();
        }
    }
}