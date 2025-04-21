package com.cinema.services;

import com.cinema.models.Ve;
import com.cinema.repositories.VeRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class VeService{

    private final VeRepository veRepository;

    public VeService(DatabaseConnection databaseConnection) {
        this.veRepository = new VeRepository(databaseConnection);
    }

    public List<Ve> getAllVe() throws SQLException {
        return veRepository.findAll();
    }

    public List<Ve> getAllVeDetail() throws SQLException {
        return veRepository.findAllDetail();
    }

    public List<Ve> findByHoaDon(Integer maHoaDon) throws SQLException {
        return veRepository.findByHoaDon(maHoaDon);
    }

    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        return veRepository.findBySoGhe(soGhe);
    }

    public Ve saveVe(Ve ve) throws SQLException {
        return veRepository.save(ve);
    }

    public Ve updateVe(Ve ve) throws SQLException {
        return veRepository.update(ve);
    }

    public void deleteVe(int maVe) throws SQLException {
        veRepository.delete(maVe);
    }

    public void updateVeStatus(int maVe, String trangThai, Integer maHoaDon) throws SQLException {
        updateVeStatus(maVe, trangThai, maHoaDon);
    }
}