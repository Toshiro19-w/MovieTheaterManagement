package com.cinema.repositories.Interface;

import java.sql.SQLException;

public interface IChiTietHoaDonRepository {
    void createChiTietHoaDon(int maHoaDon, int maVe) throws SQLException;
}
