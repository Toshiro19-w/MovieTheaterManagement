package com.cinema.repositories;

import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;


public abstract class BaseRepository<T> implements IRepository<T> {
    protected Connection conn;

    public BaseRepository(Connection conn) {
        this.conn = conn != null ? conn : DatabaseConnection.getConnection();
    }

    @Override
    public void close() {
        // Không đóng connection ở đây vì connection được quản lý tập trung bởi DatabaseConnection
        // Các repository con có thể override nếu cần xử lý đặc biệt
    }

//    protected void finalize() throws Throwable {
//        try {
//            // Đảm bảo không đóng connection ở đây
//        } finally {
//            super.finalize();
//        }
//    }
}

