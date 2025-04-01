package com.cinema.repositories;

import com.cinema.models.Phim;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IPhimRepository {
    List<Phim> findAll(int page, int pageSize);
    Optional<Phim> findById(int maPhim);
    List<Phim> findAll();
    List<Phim> findByTenPhimContaining(String keyword);
    List<Phim> findByNgayKhoiChieuBetween(LocalDate start, LocalDate end);
    Phim save(Phim phim);
    boolean deleteById(int maPhim);
}
