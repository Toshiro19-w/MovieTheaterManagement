package com.cinema.models.repositories.Interface;

import com.cinema.models.Ve;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface IVeRepository {
    List<Ve> findAllDetail() throws SQLException;
    List<Ve> findBySoGhe(String soGhe) throws SQLException;
    Ve findVeByMaVe(int maVe) throws SQLException;
    BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException;
    boolean isSuatChieuExists(int maSuatChieu) throws SQLException;
    boolean isPhongExists(int maPhong) throws SQLException;
    boolean isSeatTaken(int maSuatChieu, String soGhe) throws SQLException;
}
