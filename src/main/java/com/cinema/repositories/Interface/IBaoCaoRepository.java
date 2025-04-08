package com.cinema.repositories.Interface;

import com.cinema.models.BaoCao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IBaoCaoRepository {
    List<BaoCao> getBaoCaoDoanhThuTheoPhim(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException;
}
