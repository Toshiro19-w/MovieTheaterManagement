package com.cinema.services;

import java.sql.SQLException;
import java.util.List;

import com.cinema.models.DanhGia;
import com.cinema.repositories.DanhGiaRepository;
import com.cinema.utils.DatabaseConnection;

public class DanhGiaService {
    private final DanhGiaRepository danhGiaRepository;

    public DanhGiaService(DatabaseConnection dbConnection) {
        this.danhGiaRepository = new DanhGiaRepository(dbConnection);
    }

    // Lấy danh sách đánh giá của một phim, giới hạn số lượng
    public List<DanhGia> getDanhGiaByPhimId(int maPhim, int limit) throws SQLException {
        return danhGiaRepository.getDanhGiaByPhimId(maPhim, limit);
    }
    
    // Kiểm tra xem khách hàng đã mua vé xem phim này chưa
    public boolean daXemPhim(int maKhachHang, int maPhim) throws SQLException {
        return danhGiaRepository.daXemPhim(maKhachHang, maPhim);
    }
    
    // Lấy mã vé đã mua của khách hàng cho phim này
    public int getMaVeDaMua(int maKhachHang, int maPhim) throws SQLException {
        return danhGiaRepository.getMaVeDaMua(maKhachHang, maPhim);
    }
    
    // Kiểm tra xem khách hàng đã đánh giá phim này chưa
    public boolean daDanhGia(int maKhachHang, int maPhim) throws SQLException {
        return danhGiaRepository.daDanhGia(maKhachHang, maPhim);
    }
    
    // Thêm đánh giá mới
    public int themDanhGia(DanhGia danhGia) throws SQLException {
        return danhGiaRepository.themDanhGia(danhGia);
    }
    
    // Cập nhật đánh giá
    public boolean capNhatDanhGia(DanhGia danhGia) throws SQLException {
        return danhGiaRepository.capNhatDanhGia(danhGia);
    }
    
    // Lấy đánh giá theo ID
    public DanhGia getDanhGiaById(int maDanhGia) throws SQLException {
        return danhGiaRepository.getDanhGiaById(maDanhGia);
    }
    
    // Lấy đánh giá của người dùng cho một phim
    public DanhGia getDanhGiaByUserAndPhim(int maNguoiDung, int maPhim) throws SQLException {
        return danhGiaRepository.getDanhGiaByUserAndPhim(maNguoiDung, maPhim);
    }
    
    // Lấy điểm đánh giá trung bình của phim
    public double getDiemDanhGiaTrungBinh(int maPhim) throws SQLException {
        return danhGiaRepository.getDiemDanhGiaTrungBinh(maPhim);
    }
}