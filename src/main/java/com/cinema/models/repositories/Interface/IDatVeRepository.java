package com.cinema.models.repositories.Interface;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public interface IDatVeRepository {
    void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException;

    int confirmPayment(int maVe, int maKhachHang) throws SQLException;

    void cancelVe(int maVe) throws SQLException;

    int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException;

    BigDecimal getGiaVeFromVe(int maVe, Connection conn) throws SQLException;

    boolean isSeatTaken(int maSuatChieu, String soGhe, Connection conn) throws SQLException;
}
