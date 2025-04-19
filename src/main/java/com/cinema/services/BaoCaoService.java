package com.cinema.services;

import com.cinema.models.BaoCao;
import com.cinema.repositories.BaoCaoRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BaoCaoService {
    private final BaoCaoRepository baoCaoRepository;

    public BaoCaoService(DatabaseConnection databaseConnection) {
        this.baoCaoRepository = new BaoCaoRepository(databaseConnection);
    }

    public List<BaoCao> getBaoCaoDoanhThuTheoPhim(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return baoCaoRepository.getBaoCaoDoanhThuTheoPhim(tuNgay, denNgay);
    }
}
