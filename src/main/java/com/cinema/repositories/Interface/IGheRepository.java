package com.cinema.repositories.Interface;

import com.cinema.models.Ghe;

import java.sql.SQLException;
import java.util.List;

public interface IGheRepository {
    List<Ghe> findGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException;
}
