package com.cinema.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Quản lý giao dịch cơ sở dữ liệu
 */
public class TransactionManager {
    private final DatabaseConnection dbConnection;
    
    public TransactionManager(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    /**
     * Thực hiện một giao dịch với cơ sở dữ liệu
     * @param <T> kiểu dữ liệu trả về
     * @param operation hàm thực hiện giao dịch
     * @return kết quả của giao dịch
     * @throws SQLException nếu có lỗi SQL
     */
    public <T> T executeTransaction(Function<Connection, T> operation) throws SQLException {
        Connection connection = null;
        boolean originalAutoCommit = true;
        
        try {
            connection = dbConnection.getConnection();
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            T result = operation.apply(connection);
            
            connection.commit();
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Thực hiện một giao dịch với cơ sở dữ liệu mà không trả về kết quả
     * @param operation hàm thực hiện giao dịch
     * @throws SQLException nếu có lỗi SQL
     */
    public void executeTransactionWithoutResult(SqlConsumer<Connection> operation) throws SQLException {
        executeTransaction(connection -> {
            try {
                operation.accept(connection);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        });
    }
    
    /**
     * Interface functional cho các thao tác không trả về kết quả
     * @param <T> kiểu dữ liệu đầu vào
     */
    @FunctionalInterface
    public interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}