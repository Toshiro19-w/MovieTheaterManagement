package com.cinema.services;

import com.cinema.models.Ve;
import com.cinema.repositories.IPhimRepository;
import com.cinema.repositories.IVeRepository;
import com.cinema.repositories.VeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class VeService {
    private final IVeRepository veRepo;
    public VeService() {
        this.veRepo = new VeRepository();
    }

    public List<Ve> getAllVe(int page, int pageSize) {
        return veRepo.findAll(page, pageSize);
    }

    public Optional<Ve> getVeById(int maPhim) {
        return veRepo.findById(maPhim);
    }

//    public List<Ve> getPhimByReleaseDateRange(LocalDate start, LocalDate end) {
//        return veRepo.findByNgayKhoiChieuBetween(start, end);
//    }

    public Ve addOrUpdateVe(Ve ve) {
        if (ve.getTenPhim().isBlank()) {
            throw new IllegalArgumentException("Tên phim không được để trống");
        }
        return veRepo.save(ve);
    }

    public boolean deletePhim(int maVe) {
        return veRepo.deleteById(maVe);
    }
}
