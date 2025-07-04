package com.cinema.controllers;

import com.cinema.models.BaoCao;
import com.cinema.models.BaoCaoNhanVien;
import com.cinema.services.BaoCaoService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BaoCaoController {
    private final BaoCaoService baoCaoService;

    public BaoCaoController(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    public List<BaoCao> getBaoCaoDoanhThuTheoPhim(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return baoCaoService.getBaoCaoDoanhThuTheoPhim(tuNgay, denNgay);
    }

    public List<BaoCaoNhanVien> getBaoCaoNhanVien(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return baoCaoService.getBaoCaoNhanVien(tuNgay, denNgay);
    }
}
