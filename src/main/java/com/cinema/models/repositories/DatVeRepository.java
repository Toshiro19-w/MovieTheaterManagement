package com.cinema.models.repositories;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.repositories.Interface.IDatVeRepository;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class DatVeRepository implements IDatVeRepository {

    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public DatVeRepository(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = databaseConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
    }

    @Override
    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String checkSuatChieuSql = "SELECT soSuatChieu FROM SuatChieu WHERE maSuatChieu = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSuatChieuSql)) {
                    checkStmt.setInt(1, maSuatChieu);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            int soSuatChieu = rs.getInt("soSuatChieu");
                            if (soSuatChieu <= 0) {
                                throw new SQLException("Không còn suất chiếu khả dụng cho mã suất chiếu: " + maSuatChieu);
                            }
                        } else {
                            throw new SQLException("Suất chiếu không tồn tại: " + maSuatChieu);
                        }
                    }
                }

                if (isSeatTaken(maSuatChieu, soGhe, conn)) {
                    throw new SQLException("Ghế " + soGhe + " đã được đặt cho suất chiếu " + maSuatChieu);
                }

                String insertVeSql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement veStmt = conn.prepareStatement(insertVeSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    veStmt.setInt(1, maSuatChieu);
                    veStmt.setInt(2, maPhong);
                    veStmt.setString(3, soGhe);
                    veStmt.setBigDecimal(4, giaVe);
                    veStmt.setString(5, TrangThaiVe.BOOKED.toString());
                    veStmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    veStmt.executeUpdate();
                }

                String updateSuatChieuSql = "UPDATE SuatChieu SET soSuatChieu = soSuatChieu - 1 WHERE maSuatChieu = ?";
                try (PreparedStatement updateSuatChieuStmt = conn.prepareStatement(updateSuatChieuSql)) {
                    updateSuatChieuStmt.setInt(1, maSuatChieu);
                    updateSuatChieuStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Lỗi khi đặt vé: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public int confirmPayment(int maVe, int maKhachHang) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal giaVe = getGiaVeFromVe(maVe, conn);

                String hoaDonSql = "INSERT INTO HoaDon (maKhachHang, ngayLap, tongTien) VALUES (?, NOW(), ?)";
                int maHoaDonGenerated;
                try (PreparedStatement hoaDonStmt = conn.prepareStatement(hoaDonSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    hoaDonStmt.setInt(1, maKhachHang);
                    hoaDonStmt.setBigDecimal(2, giaVe);
                    hoaDonStmt.executeUpdate();

                    try (ResultSet generatedKeys = hoaDonStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            maHoaDonGenerated = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Không thể lấy mã hóa đơn!");
                        }
                    }
                }

                String updateVeSql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
                try (PreparedStatement updateVeStmt = conn.prepareStatement(updateVeSql)) {
                    updateVeStmt.setString(1, TrangThaiVe.PAID.toString());
                    updateVeStmt.setInt(2, maHoaDonGenerated);
                    updateVeStmt.setInt(3, maVe);
                    int affectedRows = updateVeStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Không tìm thấy vé với mã: " + maVe);
                    }
                }

                String chiTietHoaDonSql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
                try (PreparedStatement chiTietHoaDonStmt = conn.prepareStatement(chiTietHoaDonSql)) {
                    chiTietHoaDonStmt.setInt(1, maHoaDonGenerated);
                    chiTietHoaDonStmt.setInt(2, maVe);
                    chiTietHoaDonStmt.executeUpdate();
                }

                conn.commit();
                return maHoaDonGenerated;
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Lỗi khi xác nhận thanh toán: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void cancelVe(int maVe) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int maSuatChieu = getMaSuatChieuFromVe(maVe, conn);

                String deleteVeSql = "DELETE FROM Ve WHERE maVe = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteVeSql)) {
                    deleteStmt.setInt(1, maVe);
                    int affectedRows = deleteStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Không tìm thấy vé với mã: " + maVe);
                    }
                }

                String updateSuatChieuSql = "UPDATE SuatChieu SET soSuatChieu = soSuatChieu + 1 WHERE maSuatChieu = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSuatChieuSql)) {
                    updateStmt.setInt(1, maSuatChieu);
                    updateStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Lỗi khi hủy vé: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        String sql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND maHoaDon IS NULL ORDER BY ngayDat DESC LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maVe");
                }
                throw new SQLException("Không tìm thấy mã vé vừa đặt!");
            }
        }
    }

    @Override
    public BigDecimal getGiaVeFromVe(int maVe, Connection conn) throws SQLException {
        String sql = "SELECT giaVe FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }
                throw new SQLException("Không tìm thấy giá vé cho mã vé: " + maVe);
            }
        }
    }

    @Override
    public int getMaSuatChieuFromVe(int maVe, Connection conn) throws SQLException {
        String sql = "SELECT maSuatChieu FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maSuatChieu");
                }
                throw new SQLException("Không tìm thấy suất chiếu cho vé với mã: " + maVe);
            }
        }
    }

    @Override
    public boolean isSeatTaken(int maSuatChieu, String soGhe, Connection conn) throws SQLException {
        String sql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND trangThai != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}