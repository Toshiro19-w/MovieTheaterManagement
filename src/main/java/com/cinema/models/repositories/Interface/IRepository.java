package com.cinema.models.repositories.Interface;

import java.sql.SQLException;
import java.util.List;

public interface IRepository<T> {
    List<T> findAll() throws SQLException;
    T findById(int id) throws SQLException;
    T save(T entity) throws SQLException;
    T update(T entity) throws SQLException;
    void delete(int id) throws SQLException;
}