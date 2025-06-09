package com.cinema.services;

import java.sql.SQLException;
import java.util.List;

import com.cinema.models.repositories.PhimTheLoaiRepository;
import com.cinema.utils.DatabaseConnection;

/**
 * Lớp service để quản lý mối quan hệ giữa Phim và TheLoaiPhim
 */
public class PhimTheLoaiService {
    private final PhimTheLoaiRepository phimTheLoaiRepository;
    
    public PhimTheLoaiService(DatabaseConnection dbConnection) {
        this.phimTheLoaiRepository = new PhimTheLoaiRepository(dbConnection);
    }
    
    /**
     * Thêm một thể loại cho phim
     * 
     * @param maPhim Mã phim
     * @param maTheLoai Mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public void addTheLoaiForPhim(int maPhim, int maTheLoai) throws SQLException {
        phimTheLoaiRepository.addTheLoaiForPhim(maPhim, maTheLoai);
    }
    
    /**
     * Thêm nhiều thể loại cho phim
     * 
     * @param maPhim Mã phim
     * @param maTheLoaiList Danh sách mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public void addTheLoaisForPhim(int maPhim, List<Integer> maTheLoaiList) throws SQLException {
        phimTheLoaiRepository.addTheLoaisForPhim(maPhim, maTheLoaiList);
    }
    
    /**
     * Xóa tất cả thể loại của một phim
     * 
     * @param maPhim Mã phim
     * @throws SQLException Nếu có lỗi SQL
     */
    public void deleteAllTheLoaiOfPhim(int maPhim) throws SQLException {
        phimTheLoaiRepository.deleteAllTheLoaiOfPhim(maPhim);
    }
    
    /**
     * Lấy danh sách mã thể loại của một phim
     * 
     * @param maPhim Mã phim
     * @return Danh sách mã thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<Integer> getTheLoaiIdsByPhimId(int maPhim) throws SQLException {
        return phimTheLoaiRepository.getTheLoaiIdsByPhimId(maPhim);
    }
    
    /**
     * Lấy danh sách tên thể loại của một phim
     * 
     * @param maPhim Mã phim
     * @return Danh sách tên thể loại
     * @throws SQLException Nếu có lỗi SQL
     */
    public List<String> getTheLoaiNamesByPhimId(int maPhim) throws SQLException {
        return phimTheLoaiRepository.getTheLoaiNamesByPhimId(maPhim);
    }
    
    /**
     * Lấy danh sách tên thể loại của một phim dưới dạng chuỗi phân cách bởi dấu phẩy
     * 
     * @param maPhim Mã phim
     * @return Chuỗi tên thể loại phân cách bởi dấu phẩy
     * @throws SQLException Nếu có lỗi SQL
     */
    public String getTheLoaiNamesStringByPhimId(int maPhim) throws SQLException {
        return phimTheLoaiRepository.getTheLoaiNamesStringByPhimId(maPhim);
    }
    
    /**
     * Cập nhật thể loại cho phim
     * 
     * @param maPhim Mã phim
     * @param maTheLoaiList Danh sách mã thể loại mới
     * @throws SQLException Nếu có lỗi SQL
     */
    public void updateTheLoaisForPhim(int maPhim, List<Integer> maTheLoaiList) throws SQLException {
        phimTheLoaiRepository.updateTheLoaisForPhim(maPhim, maTheLoaiList);
    }
}