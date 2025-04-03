package com.cinema.repositories;

import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IVeRepository{
    List<Ve> findAll() throws SQLException; // Thêm phương thức findAll()
    Ve findByMaVe(int maVe) throws SQLException;

    Ve save(Ve ve) throws SQLException;
    Ve update(Ve ve) throws SQLException;
    void delete(int maVe) throws SQLException;
}
