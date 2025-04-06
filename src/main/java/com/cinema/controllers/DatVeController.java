package com.cinema.controllers;

import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;
import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.repositories.GheRepository;
import com.cinema.repositories.SuatChieuRepository;
import com.cinema.repositories.VeRepository;
import com.cinema.services.GheService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DatVeController {
    private final SuatChieuService suatChieuService;
    private final GheService gheService;
    private final VeService veService;

    public DatVeController(SuatChieuService suatChieuService, GheService gheService, VeService veService) {
        this.suatChieuService = suatChieuService;
        this.gheService = gheService;
        this.veService = veService;
    }

    public List<SuatChieu> getSuatChieuByPhim(int maPhim) throws SQLException {
        return suatChieuService.findByMaPhim(maPhim);
    }

    public List<Ghe> getGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException {
        return gheService.findGheTrongByPhongAndSuatChieu(maPhong, maSuatChieu);
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe) throws SQLException {
        Ve ve = new Ve(0, maSuatChieu,
                maPhong, soGhe, null,
                giaVe, TrangThaiVe.BOOKED, LocalDateTime.now()
        );
        veService.saveVe(ve);
    }
}