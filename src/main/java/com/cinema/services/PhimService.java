package com.cinema.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.cinema.models.Phim;
import com.cinema.models.dto.PaginationResult;
import com.cinema.models.repositories.PhimRepository;
import com.cinema.models.repositories.TheLoaiRepository;
import com.cinema.utils.DatabaseConnection;

public class PhimService {
    private final TheLoaiRepository theLoaiRepository;
    private final PhimRepository phimRepository;

    public PhimService(DatabaseConnection dbConnection) throws SQLException {
        dbConnection.getConnection();
        this.phimRepository = new PhimRepository(dbConnection);
        this.theLoaiRepository = new TheLoaiRepository(dbConnection);
    }

    // Lấy danh sách tất cả phim
    public List<Phim> getAllPhim() throws SQLException {
        return phimRepository.findAll();
    }

    // Lấy danh sách phim theo phân trang
    public PaginationResult<Phim> getAllPhimPaginated(int page, int pageSize) throws SQLException {
        return phimRepository.findAllPaginated(page, pageSize);
    }

    // Lấy thông tin phim theo mã phim
    public Phim getPhimById(int maPhim) throws SQLException {
        return phimRepository.findById(maPhim);
    }

    // Thêm phim mới, trả về phim đã lưu (có mã)
    public Phim addPhim(Phim phim) throws SQLException {
        return phimRepository.save(phim);
    }

    // Cập nhật phim
    public void updatePhim(Phim phim) throws SQLException {
        phimRepository.update(phim);
    }

    // Xóa phim
    public void deletePhim(int maPhim) throws SQLException {
        phimRepository.delete(maPhim);
    }

    // Lấy danh sách thể loại (tên)
    public List<String> getAllTheLoai() throws SQLException {
        return phimRepository.getAllTheLoai();
    }

    // Lấy danh sách thể loại (map id - tên)
    public Map<Integer, String> getAllTheLoaiMap() throws SQLException {
        return theLoaiRepository.getAllTheLoaiMap();
    }

    // Lấy mã thể loại từ tên thể loại
    public int getMaTheLoaiByTen(String tenTheLoai) throws SQLException {
        return phimRepository.getMaTheLoaiByTen(tenTheLoai);
    }

    // Lấy danh sách định dạng phim
    public List<String> getAllDinhDang() throws SQLException {
        return phimRepository.getAllDinhDang();
    }

    // Kiểm tra tên phim đã tồn tại chưa (trừ mã phim truyền vào)
    public boolean isMovieTitleExists(String tenPhim, int excludeMaPhim) throws SQLException {
        return phimRepository.isMovieTitleExists(tenPhim, excludeMaPhim);
    }
    

    // Quản lý thể loại
    public void addTheLoai(String newGenre) throws SQLException {
        theLoaiRepository.addTheLoai(newGenre);
    }

    public void updateTheLoai(int genreId, String updatedGenre) throws SQLException {
        theLoaiRepository.updateTheLoai(genreId, updatedGenre);
    }

    public void deleteTheLoai(int genreId) throws SQLException {
        theLoaiRepository.deleteTheLoai(genreId);
    }

    // Lấy danh sách phim theo tên phòng (tùy nhu cầu)
    public List<Phim> getPhimByTenPhong(String tenPhong) throws SQLException {
        return phimRepository.getPhimByTenPhong(tenPhong);
    }

    // Lấy danh sách phim đang chiếu
    public List<Phim> getPhimDangChieu() throws SQLException {
        return phimRepository.findPhimDangChieu();
    }
}