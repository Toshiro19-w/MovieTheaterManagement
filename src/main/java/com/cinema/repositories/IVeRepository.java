package com.cinema.repositories;

import com.cinema.models.Ve;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IVeRepository {
    // Lấy tất cả vé phân trang
    List<Ve> findAll(int page, int pageSize);

    // Lấy tất cả vé không phân trang
    List<Ve> findAll();

    // Tìm vé theo mã vé
    Optional<Ve> findById(int maVe);

    // Tìm vé theo mã suất chiếu
    List<Ve> findByMaSuatChieu(int maSuatChieu);

    // Tìm vé theo khoảng thời gian đặt vé
    List<Ve> findByNgayDatBetween(LocalDateTime start, LocalDateTime end);

    // Tìm vé theo trạng thái
    List<Ve> findByTrangThai(String trangThai);

    // Tìm vé theo mã khách hàng
    List<Ve> findByMaKhachHang(int maKhachHang);

    // Tìm vé theo mã hóa đơn
    List<Ve> findByMaHoaDon(int maHoaDon);

    // Lưu/thêm mới vé
    Ve save(Ve ve);

    // Xóa vé theo mã vé
    boolean deleteById(int maVe);

    // Cập nhật trạng thái vé
    boolean updateTrangThai(int maVe, String trangThai);

    // Đếm số vé theo trạng thái
    long countByTrangThai(String trangThai);
}
