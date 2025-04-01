package com.cinema.services;

import com.cinema.models.Phim;
import com.cinema.repositories.IPhimRepository;
import com.cinema.repositories.PhimRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PhimService {
    private final IPhimRepository phimRepo;

    public PhimService() {
        this.phimRepo = new PhimRepository();
    }

    public List<Phim> getAllPhim(int page, int pageSize) {
        return phimRepo.findAll(page, pageSize);
    }

    public Optional<Phim> getPhimById(int maPhim) {
        return phimRepo.findById(maPhim);
    }

    public List<Phim> searchPhimByName(String keyword) {
        return phimRepo.findByTenPhimContaining(keyword);
    }

    public List<Phim> getPhimByReleaseDateRange(LocalDate start, LocalDate end) {
        return phimRepo.findByNgayKhoiChieuBetween(start, end);
    }

    public Phim addOrUpdatePhim(Phim phim) {
        if (phim.getTenPhim().isBlank()) {
            throw new IllegalArgumentException("Tên phim không được để trống");
        }
        return phimRepo.save(phim);
    }

    public boolean deletePhim(int maPhim) {
        return phimRepo.deleteById(maPhim);
    }
}
