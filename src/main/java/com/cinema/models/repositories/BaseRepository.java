package com.cinema.models.repositories;

import com.cinema.models.repositories.Interface.IRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseRepository<T> implements IRepository<T> {
    protected final DatabaseConnection dbConnection;

    public BaseRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
    }
    
    protected Connection getConnection() throws SQLException {
        return dbConnection.getConnection();
    }
}