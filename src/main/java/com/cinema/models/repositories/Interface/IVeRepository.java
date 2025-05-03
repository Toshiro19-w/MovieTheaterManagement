package com.cinema.models.repositories.Interface;

import com.cinema.models.Ve;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IVeRepository {
    List<Ve> findAllDetail() throws SQLException;
    List<Ve> findBySoGhe(String soGhe) throws SQLException;
    void cancelVe(int maVe) throws SQLException;
    void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException;
    int confirmPayment(int maVe, int maKhachHang) throws SQLException;
    BigDecimal getGiaVeFromVe(int maVe, Connection conn) throws SQLException;
    BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException;
    int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException;
    int getMaHoaDonFromVe(int maVe) throws SQLException;
    boolean isSuatChieuExists(int maSuatChieu) throws SQLException;
    boolean isPhongExists(int maPhong) throws SQLException;
    boolean isSeatTaken(int maSuatChieu, String soGhe) throws SQLException;
}
