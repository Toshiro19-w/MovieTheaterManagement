package com.cinema.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.KhachHang;
import com.cinema.models.Ve;
import com.cinema.models.dto.PaginationResult;
import com.cinema.models.repositories.DatVeRepository;
import com.cinema.models.repositories.KhachHangRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.utils.DatabaseConnection;

public class VeService {
    private final VeRepository veRepository;
    private final KhachHangRepository khachHangRepository;
    private final DatVeRepository datVeRepository;
    
    public VeService(DatabaseConnection databaseConnection) {
        this.veRepository = new VeRepository(databaseConnection);
        this.khachHangRepository = new KhachHangRepository(databaseConnection);
        this.datVeRepository = new DatVeRepository(databaseConnection);
    }

    public List<Ve> getAllVe() throws SQLException {
        return veRepository.findAll();
    }
    
    public PaginationResult<Ve> getAllVePaginated(int page, int pageSize) throws SQLException {
        return veRepository.findAllPaginated(page, pageSize);
    }
    
    public Ve findVeByMaVe(int maVe) throws SQLException {
        return veRepository.findById(maVe);
    }

    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        return veRepository.findBySoGhe(soGhe);
    }

    public Ve saveVe(Ve ve, String tenPhong, String tenKhuyenMai, LocalDateTime ngayGioChieu) throws SQLException {
        Integer maSuatChieu = getMaSuatChieu(ngayGioChieu, tenPhong);
        if (maSuatChieu == null) {
            throw new SQLException("Không tìm thấy suất chiếu phù hợp");
        }
        ve.setMaSuatChieu(maSuatChieu);

        if (tenKhuyenMai != null && !tenKhuyenMai.isEmpty() && !tenKhuyenMai.equals("Không có")) {
            Integer maKhuyenMai = getMaKhuyenMai(tenKhuyenMai);
            if (maKhuyenMai == null) {
                throw new SQLException("Khuyến mãi không hợp lệ hoặc đã hết hạn");
            }
            ve.setMaKhuyenMai(maKhuyenMai);
        } else {
            ve.setMaKhuyenMai(0);
        }

        if (ve.getTrangThai() == null) {
            ve.setTrangThai(TrangThaiVe.BOOKED);
        }

        // Tính toán giá vé trước khi lưu
        return veRepository.save(ve);
    }

    public Ve updateVe(Ve ve, String tenPhong, String tenKhuyenMai, LocalDateTime ngayGioChieu) throws SQLException {
        Integer maSuatChieu = getMaSuatChieu(ngayGioChieu, tenPhong);
        if (maSuatChieu == null) {
            throw new SQLException("Không tìm thấy suất chiếu phù hợp");
        }
        ve.setMaSuatChieu(maSuatChieu);

        // Kiểm tra nếu vé đã có khuyến mãi hết hạn
        if (ve.getMaKhuyenMai() != 0) {
            String currentKhuyenMai = getTenKhuyenMaiByMa(ve.getMaKhuyenMai());
            if (currentKhuyenMai != null && !isPromotionValid(currentKhuyenMai)) {
                throw new SQLException("Không thể chỉnh sửa vé vì khuyến mãi đã hết hạn");
            }
        }

        if (tenKhuyenMai != null && !tenKhuyenMai.isEmpty() && !tenKhuyenMai.equals("Không có")) {
            Integer maKhuyenMai = getMaKhuyenMai(tenKhuyenMai);
            if (maKhuyenMai == null) {
                throw new SQLException("Khuyến mãi không hợp lệ hoặc đã hết hạn");
            }
            ve.setMaKhuyenMai(maKhuyenMai);
        } else {
            ve.setMaKhuyenMai(0);
        }

        // Tính lại giá vé trước khi cập nhật
        return veRepository.update(ve);
    }
    
    public void deleteVe(int maVe) throws SQLException {
        veRepository.delete(maVe);
    }

    public KhachHang getKhachHangByMaVe(int maVe) throws SQLException {
        return khachHangRepository.getKhachHangByMaVe(maVe);
    }

    public BigDecimal getTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        return veRepository.findTicketPriceBySuatChieu(maSuatChieu);
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang, int maNhanVien) throws SQLException {
        datVeRepository.datVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang, maNhanVien);
    }
    
    public void createPendingVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        datVeRepository.createPendingVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang);
    }

    public int confirmPayment(int maVe, int maKhachHang, int maNhanVien) throws SQLException {
        return datVeRepository.confirmPayment(maVe, maKhachHang, maNhanVien);
    }

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return datVeRepository.getMaVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }
    
    public int getPendingVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return datVeRepository.getPendingVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }

    public void cancelVe(int maVe) throws SQLException {
        datVeRepository.cancelVe(maVe);
    }

    public Integer getMaSuatChieu(LocalDateTime ngayGioChieu, String tenPhong) throws SQLException {
        return veRepository.getMaSuatChieuByNgayGioAndTenPhong(ngayGioChieu, tenPhong);
    }

    public Integer getMaGhe(String soGhe, String tenPhong) throws SQLException {
        return veRepository.getMaGheByTenPhong(soGhe, tenPhong);
    }

    public Integer getMaKhuyenMai(String tenKhuyenMai) throws SQLException {
        return veRepository.getMaKhuyenMaiByTen(tenKhuyenMai);
    }

    public String getTenKhuyenMaiByMa(int maKhuyenMai) throws SQLException {
        return veRepository.getTenKhuyenMaiByMa(maKhuyenMai);
    }

    public boolean isPromotionValid(String tenKhuyenMai) throws SQLException {
        return veRepository.isPromotionValid(tenKhuyenMai);
    }

    public List<String> getValidPromotions() throws SQLException {
        return veRepository.getValidPromotions();
    }

    public boolean isGheDaDat(int maSuatChieu, String soGhe) throws SQLException {
        return veRepository.isSeatTaken(maSuatChieu, soGhe);
    }
    
    public Ve findVeBySuatChieuAndSoGhe(int maSuatChieu, String soGhe) throws SQLException {
        return veRepository.findBySuatChieuAndSoGhe(maSuatChieu, soGhe);
    }
}
