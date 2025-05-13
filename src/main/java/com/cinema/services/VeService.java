package com.cinema.services;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

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

    public Ve saveVe(Ve ve) throws SQLException {
        return veRepository.save(ve);
    }

    public Ve updateVe(Ve ve) throws SQLException {
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

    public boolean isGheDaDat(int maSuatChieu, String soGhe) throws SQLException {
        String sql = """
                SELECT v.maVe 
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                WHERE v.maSuatChieu = ? 
                AND g.soGhe = ? 
                AND v.trangThai != 'CANCELLED'""";
                
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}