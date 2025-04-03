package com.cinema.repositories;

import com.cinema.models.TheLoaiPhim;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TheLoaiPhimRepository extends BaseRepository<TheLoaiPhim> {
    public TheLoaiPhimRepository(Connection conn) {
        super(conn);
    }

    @Override
    public List<TheLoaiPhim> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public TheLoaiPhim findById(int id) throws SQLException {
        return null;
    }

    @Override
    public TheLoaiPhim save(TheLoaiPhim entity) throws SQLException {
        return null;
    }

    @Override
    public TheLoaiPhim update(TheLoaiPhim entity) throws SQLException {
        return null;
    }

    @Override
    public void delete(int id) throws SQLException {

    }
}