package com.cinema.models.repositories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.repositories.Interface.IDatVeRepository;
import com.cinema.utils.DatabaseConnection;

public class DatVeRepository implements IDatVeRepository {

    protected Connection conn;
    protected DatabaseConnection dbConnection;
    private VeRepository veRepository;

    public DatVeRepository(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = databaseConnection;
        try {
            this.conn = dbConnection.getConnection();
            this.veRepository = new VeRepository(dbConnection);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
    }

    @Override
    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Kiểm tra xem suất chiếu có tồn tại không
                if (!veRepository.isSuatChieuExists(maSuatChieu)) {
                    throw new SQLException("Suất chiếu không tồn tại: " + maSuatChieu);
                }

                // Kiểm tra xem ghế đã được đặt chưa
                if (veRepository.isSeatTaken(maSuatChieu, soGhe)) {
                    throw new SQLException("Ghế " + soGhe + " đã được đặt cho suất chiếu " + maSuatChieu);
                }

                // Lấy mã ghế từ số ghế và mã suất chiếu
                int maGhe = veRepository.getMaGheFromSoGhe(soGhe, maSuatChieu);
                if (maGhe <= 0) {
                    throw new SQLException("Không tìm thấy ghế " + soGhe + " trong phòng chiếu");
                }

                // Lấy mã giá vé từ mã ghế
                int maGiaVe = veRepository.getMaGiaVeFromMaGhe(maGhe);
                if (maGiaVe <= 0) {
                    throw new SQLException("Không tìm thấy giá vé cho ghế " + soGhe);
                }

                // Thêm vé mới
                String insertVeSql = "INSERT INTO Ve (maSuatChieu, maGhe, maGiaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement veStmt = conn.prepareStatement(insertVeSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    veStmt.setInt(1, maSuatChieu);
                    veStmt.setInt(2, maGhe);
                    veStmt.setInt(3, maGiaVe);
                    veStmt.setString(4, TrangThaiVe.BOOKED.toString());
                    veStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    veStmt.executeUpdate();
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
                }                String updateVeSql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
                try (PreparedStatement updateVeStmt = conn.prepareStatement(updateVeSql)) {
                    updateVeStmt.setString(1, TrangThaiVe.PAID.getValue());
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
                // Cập nhật trạng thái vé thành cancelled thay vì xóa
                String updateVeSql = "UPDATE Ve SET trangThai = 'cancelled' WHERE maVe = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateVeSql)) {
                    updateStmt.setInt(1, maVe);
                    int affectedRows = updateStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Không tìm thấy vé với mã: " + maVe);
                    }
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
        // Sửa lại để sử dụng maGhe thay vì soGhe
        String sql = """
            SELECT v.maVe 
            FROM Ve v
            JOIN Ghe g ON v.maGhe = g.maGhe
            WHERE v.maSuatChieu = ? 
            AND g.soGhe = ? 
            AND v.maHoaDon IS NULL 
            AND v.trangThai = 'booked'
            ORDER BY v.ngayDat DESC 
            LIMIT 1""";
            
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
        // Sửa lại để lấy giá vé từ bảng GiaVe thông qua maGiaVe
        String sql = """
            SELECT gv.giaVe 
            FROM Ve v
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            WHERE v.maVe = ?""";
            
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

    // Sửa lại phương thức isSeatTaken để sử dụng maGhe thay vì soGhe
    @Override
    public boolean isSeatTaken(int maSuatChieu, String soGhe, Connection conn) throws SQLException {
        String sql = """
            SELECT v.maVe 
            FROM Ve v
            JOIN Ghe g ON v.maGhe = g.maGhe
            WHERE v.maSuatChieu = ? 
            AND g.soGhe = ? 
            AND v.trangThai NOT IN ('cancelled', 'deleted')""";
            
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}