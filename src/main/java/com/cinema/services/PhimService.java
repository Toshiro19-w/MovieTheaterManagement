package com.cinema.services;

import com.cinema.models.Phim;
import com.cinema.repositories.PhimRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PhimService {
    private final PhimRepository phimRepo;

    public PhimService(Connection conn) {
        this.phimRepo = new PhimRepository(conn);
    }

    public List<Phim> getAllPhim() throws SQLException {
        return phimRepo.findAll();
    }

    public Phim getPhimById(int maPhim) throws SQLException {
        return phimRepo.findById(maPhim);
    }

    public Phim addPhim(Phim phim) throws SQLException {
        return phimRepo.save(phim);
    }

    public Phim updatePhim(Phim phim) throws SQLException {
        return phimRepo.update(phim);
    }

    public void deletePhim(int maPhim) throws SQLException {
        phimRepo.delete(maPhim);
    }
}
