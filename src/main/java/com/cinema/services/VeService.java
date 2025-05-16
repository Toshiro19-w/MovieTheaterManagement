package com.cinema.services;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.KhachHang;
import com.cinema.models.Ve;
import com.cinema.models.repositories.DatVeRepository;
import com.cinema.models.repositories.KhachHangRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.utils.DatabaseConnection;

public class VeService {
    private final VeRepository veRepository;
    private final KhachHangRepository khachHangRepository;
    private final DatVeRepository datVeRepository;
    private final DatabaseConnection databaseConnection;

    public VeService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        this.veRepository = new VeRepository(databaseConnection);
        this.khachHangRepository = new KhachHangRepository(databaseConnection);
        this.datVeRepository = new DatVeRepository(databaseConnection);
    }

    public List<Ve> getAllVeDetail() throws SQLException {
        return veRepository.findAllDetail();
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

        calculateTicketPrices(ve, maSuatChieu, tenKhuyenMai);
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

        calculateTicketPrices(ve, maSuatChieu, tenKhuyenMai);
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

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        datVeRepository.datVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang);
    }

    public int confirmPayment(int maVe, int maKhachHang) throws SQLException {
        return datVeRepository.confirmPayment(maVe, maKhachHang);
    }

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return datVeRepository.getMaVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }

    public void cancelVe(int maVe) throws SQLException {
        datVeRepository.cancelVe(maVe);
    }

    public Integer getMaSuatChieu(LocalDateTime ngayGioChieu, String tenPhong) throws SQLException {
        String sql = """
                SELECT maSuatChieu 
                FROM SuatChieu sc 
                JOIN PhongChieu pc ON sc.maPhong = pc.maPhong 
                WHERE sc.ngayGioChieu = ? AND pc.tenPhong = ?""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(ngayGioChieu));
            stmt.setString(2, tenPhong);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maSuatChieu");
            }
            return null;
        }
    }

    public Integer getMaGhe(String soGhe, String tenPhong) throws SQLException {
        String sql = """
                SELECT maGhe 
                FROM Ghe g 
                JOIN PhongChieu pc ON g.maPhong = pc.maPhong 
                WHERE g.soGhe = ? AND pc.tenPhong = ?""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, soGhe);
            stmt.setString(2, tenPhong);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maGhe");
            }
            return null;
        }
    }

    public Integer getMaKhuyenMai(String tenKhuyenMai) throws SQLException {
        String sql = """
                SELECT maKhuyenMai 
                FROM KhuyenMai 
                WHERE tenKhuyenMai = ? AND trangThai = 'HoatDong' 
                AND NOW() BETWEEN ngayBatDau AND ngayKetThuc""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, tenKhuyenMai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maKhuyenMai");
            }
            return null;
        }
    }

    public String getTenKhuyenMaiByMa(int maKhuyenMai) throws SQLException {
        String sql = "SELECT tenKhuyenMai FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, maKhuyenMai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tenKhuyenMai");
            }
            return null;
        }
    }

    public boolean isPromotionValid(String tenKhuyenMai) throws SQLException {
        String sql = """
                SELECT COUNT(*) 
                FROM KhuyenMai 
                WHERE tenKhuyenMai = ? AND trangThai = 'HoatDong' 
                AND NOW() BETWEEN ngayBatDau AND ngayKetThuc""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, tenKhuyenMai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public List<String> getValidPromotions() throws SQLException {
        List<String> promotions = new ArrayList<>();
        promotions.add("Không có"); // Default option
        String sql = """
                SELECT tenKhuyenMai 
                FROM KhuyenMai 
                WHERE trangThai = 'HoatDong' 
                AND NOW() BETWEEN ngayBatDau AND ngayKetThuc""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                promotions.add(rs.getString("tenKhuyenMai"));
            }
        }
        return promotions;
    }

    public boolean isGheDaDat(int maSuatChieu, String soGhe) throws SQLException {
        return veRepository.isSeatTaken(maSuatChieu, soGhe);
    }

    public BigDecimal getGiaVeBySoGheAndSuatChieu(String soGhe, int maSuatChieu) throws SQLException {
        String sql = """
                SELECT gv.giaVe 
                FROM Ghe g
                JOIN SuatChieu sc ON g.maPhong = sc.maPhong
                JOIN GiaVe gv ON g.loaiGhe = gv.loaiGhe
                WHERE g.soGhe = ? AND sc.maSuatChieu = ?
                AND gv.ngayApDung <= NOW()
                ORDER BY gv.ngayApDung DESC
                LIMIT 1""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, soGhe);
            stmt.setInt(2, maSuatChieu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BigDecimal giaVe = rs.getBigDecimal("giaVe");
                if (giaVe == null || giaVe.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new SQLException("Giá vé không hợp lệ cho ghế " + soGhe);
                }
                return giaVe;
            }
            throw new SQLException("Không tìm thấy giá vé cho ghế " + soGhe + " và suất chiếu " + maSuatChieu);
        }
    }

    public BigDecimal getDiscountAmount(String tenKhuyenMai, BigDecimal giaVeGoc) throws SQLException {
        if (tenKhuyenMai == null || tenKhuyenMai.isEmpty() || tenKhuyenMai.equals("Không có")) {
            return BigDecimal.ZERO;
        }
        String sql = """
                SELECT giaTriGiam 
                FROM KhuyenMai 
                WHERE tenKhuyenMai = ? AND trangThai = 'HoatDong' 
                AND NOW() BETWEEN ngayBatDau AND ngayKetThuc""";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, tenKhuyenMai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BigDecimal discount = rs.getBigDecimal("giaTriGiam");
                if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Giá trị giảm giá không hợp lệ cho khuyến mãi " + tenKhuyenMai);
                }
                return discount.min(giaVeGoc); // Không giảm quá giá vé gốc
            }
            return BigDecimal.ZERO;
        }
    }

    public void calculateTicketPrices(Ve ve, int maSuatChieu, String tenKhuyenMai) throws SQLException {
        if (ve == null || ve.getSoGhe() == null) {
            throw new SQLException("Thông tin vé hoặc số ghế không hợp lệ");
        }

        BigDecimal giaVeGoc = getGiaVeBySoGheAndSuatChieu(ve.getSoGhe(), maSuatChieu);
        BigDecimal tienGiam = getDiscountAmount(tenKhuyenMai, giaVeGoc);
        BigDecimal giaVeSauGiam = giaVeGoc.subtract(tienGiam);

        // Kiểm tra tính hợp lệ của giá vé
        if (giaVeGoc.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Giá vé gốc phải lớn hơn 0");
        }
        if (tienGiam.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Tiền giảm không được âm");
        }
        if (giaVeSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Giá vé sau giảm không được âm");
        }

        ve.setGiaVeGoc(giaVeGoc);
        ve.setTienGiam(tienGiam);
        ve.setGiaVeSauGiam(giaVeSauGiam);
    }
}