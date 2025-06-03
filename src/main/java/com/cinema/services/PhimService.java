package com.cinema.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cinema.models.Phim;
import com.cinema.models.repositories.PhimRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PaginationResult;

public class PhimService {
    private Connection connection;
    private PhimTheLoaiService phimTheLoaiService;
    private PhimRepository phimRepository;
    
    public PhimService(DatabaseConnection dbConnection) throws SQLException {
        this.connection = dbConnection.getConnection();
        this.phimTheLoaiService = new PhimTheLoaiService(dbConnection);
        this.phimRepository = new PhimRepository(dbConnection);
    }
    
    /**
     * Lấy danh sách tất cả phim
     * 
     * @return Danh sách phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<Phim> getAllPhim() throws SQLException {
        return phimRepository.findAll();
    }
    
    /**
     * Lấy danh sách phim theo phân trang
     * 
     * @param page Trang hiện tại
     * @param pageSize Số lượng phim trên mỗi trang
     * @return Kết quả phân trang
     * @throws SQLException Nếu có lỗi SQL
     */
    public PaginationResult<Phim> getAllPhimPaginated(int page, int pageSize) throws SQLException {
        return phimRepository.findAllPaginated(page, pageSize);
    }
    
    /**
     * Lấy thông tin phim theo mã phim
     * 
     * @param maPhim Mã phim
     * @return Thông tin phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public Phim getPhimById(int maPhim) throws SQLException {
        return phimRepository.findById(maPhim);
    }
    
    /**
     * Thêm phim mới
     * 
     * @param phim Thông tin phim
     * @return Mã phim mới
     * @throws SQLException Nếu có lỗi SQL
     */
    public int addPhim(Phim phim) throws SQLException {
        Phim savedPhim = phimRepository.save(phim);
        return savedPhim.getMaPhim();
    }
    
    /**
     * Cập nhật thông tin phim
     * 
     * @param phim Thông tin phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public void updatePhim(Phim phim) throws SQLException {
        phimRepository.update(phim);
    }
    
    /**
     * Xóa phim
     * 
     * @param maPhim Mã phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public void deletePhim(int maPhim) throws SQLException {
        phimRepository.delete(maPhim);
    }
    
    /**
     * Lấy danh sách tất cả thể loại phim
     * 
     * @return Danh sách tên thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<String> getAllTheLoai() throws SQLException {
        return phimRepository.getAllTheLoai();
    }
    
    /**
     * Lấy danh sách tất cả định dạng phim
     * 
     * @return Danh sách định dạng phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<String> getAllDinhDang() throws SQLException {
        return phimRepository.getAllDinhDang();
    }
    
    /**
     * Lấy mã thể loại từ tên thể loại
     * 
     * @param tenTheLoai Tên thể loại
     * @return Mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public int getMaTheLoaiByTen(String tenTheLoai) throws SQLException {
        return phimRepository.getMaTheLoaiByTen(tenTheLoai);
    }
    
    /**
     * Lấy danh sách tất cả thể loại phim với mã và tên
     * 
     * @return Map chứa mã thể loại và tên thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public Map<Integer, String> getAllTheLoaiMap() throws SQLException {
        Map<Integer, String> theLoaiMap = new HashMap<>();
        String sql = "SELECT maTheLoai, tenTheLoai FROM TheLoaiPhim ORDER BY tenTheLoai";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                theLoaiMap.put(rs.getInt("maTheLoai"), rs.getString("tenTheLoai"));
            }
        }
        
        return theLoaiMap;
    }
    
    /**
     * Lấy danh sách phim theo tên phòng
     * 
     * @param tenPhong Tên phòng
     * @return Danh sách phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<Phim> getPhimByTenPhong(String tenPhong) throws SQLException {
        return phimRepository.getPhimByTenPhong(tenPhong);
    }
    
    /**
     * Kiểm tra xem tên phim đã tồn tại chưa
     * 
     * @param tenPhim Tên phim
     * @param excludeMaPhim Mã phim cần loại trừ
     * @return true nếu tên phim đã tồn tại, false nếu chưa
     * @throws SQLException Nếu có lỗi SQL
     */
    public boolean isMovieTitleExists(String tenPhim, int excludeMaPhim) throws SQLException {
        return phimRepository.isMovieTitleExists(tenPhim, excludeMaPhim);
    }
}