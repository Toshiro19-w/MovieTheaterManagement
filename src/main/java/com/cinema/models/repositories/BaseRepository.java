package com.cinema.models.repositories;

import com.cinema.models.repositories.Interface.IRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseRepository<T> implements IRepository<T> {
    protected Connection conn;
    protected final DatabaseConnection dbConnection;

    public BaseRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
    }
}