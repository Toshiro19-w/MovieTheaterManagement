package com.cinema.models.repositories.Interface;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.cinema.models.Ve;

public interface IVeRepository extends IRepository<Ve> {
    List<Ve> findAll() throws SQLException;
    List<Ve> findAllDetail() throws SQLException;
    List<Ve> findBySoGhe(String soGhe) throws SQLException;
    Ve findVeByMaVe(int maVe) throws SQLException;
    Ve save(Ve ve) throws SQLException;
    Ve update(Ve ve) throws SQLException;
    void delete(int id) throws SQLException;
    BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException;
    boolean isSuatChieuExists(int maSuatChieu) throws SQLException;
    boolean isSeatTaken(int maSuatChieu, String soGhe) throws SQLException;
}
