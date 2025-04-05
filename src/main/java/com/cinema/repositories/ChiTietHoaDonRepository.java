package com.cinema.repositories;

import com.cinema.models.ChiTietHoaDon;
import com.cinema.utils.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ChiTietHoaDonRepository extends BaseRepository<ChiTietHoaDon> {
    public ChiTietHoaDonRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    public void createChiTietHoaDon(int maHoaDon, int maVe) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHoaDon);
            stmt.setInt(2, maVe);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<ChiTietHoaDon> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public ChiTietHoaDon findById(int id) throws SQLException {
        return null;
    }

    @Override
    public ChiTietHoaDon save(ChiTietHoaDon entity) throws SQLException {
        return null;
    }

    @Override
    public ChiTietHoaDon update(ChiTietHoaDon entity) throws SQLException {
        return null;
    }

    @Override
    public void delete(int id) throws SQLException {

    }
}