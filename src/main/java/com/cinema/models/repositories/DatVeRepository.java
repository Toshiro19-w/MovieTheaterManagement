package com.cinema.models.repositories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.repositories.Interface.IDatVeRepository;
import com.cinema.utils.DatabaseConnection;

public class DatVeRepository implements IDatVeRepository {

    protected DatabaseConnection dbConnection;
    private VeRepository veRepository;

    public DatVeRepository(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = databaseConnection;
        this.veRepository = new VeRepository(dbConnection);
    }

    @Override
    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang, int maNhanVien) throws SQLException {
        // Phương thức này giờ chỉ được gọi khi thanh toán thành công
        // Nó sẽ cập nhật trạng thái của vé từ PENDING sang BOOKED
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Lấy mã ghế từ số ghế và mã suất chiếu
            int maGhe = veRepository.getMaGheFromSoGhe(soGhe, maSuatChieu);
            if (maGhe <= 0) {
                throw new SQLException("Không tìm thấy ghế " + soGhe + " trong phòng chiếu");
            }
            
            // Kiểm tra xem có vé đang ở trạng thái PENDING cho ghế này không
            String checkPendingSql = """
                SELECT maVe 
                FROM Ve 
                WHERE maSuatChieu = ? 
                AND maGhe = ? 
                AND trangThai = 'pending'""";
            
            PreparedStatement checkStmt = conn.prepareStatement(checkPendingSql);
            checkStmt.setInt(1, maSuatChieu);
            checkStmt.setInt(2, maGhe);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Nếu có vé đang ở trạng thái PENDING, cập nhật trạng thái thành BOOKED
                int maVe = rs.getInt("maVe");
                String updateSql = "UPDATE Ve SET trangThai = ? WHERE maVe = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, TrangThaiVe.BOOKED.toString());
                updateStmt.setInt(2, maVe);
                updateStmt.executeUpdate();
                updateStmt.close();
                rs.close();
                checkStmt.close();
            } else {
                rs.close();
                checkStmt.close();
                throw new SQLException("Không tìm thấy vé đang chờ thanh toán cho ghế " + soGhe);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ghi log lỗi rollback nếu cần
                }
            }
            throw new SQLException("Lỗi khi đặt vé: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ghi log lỗi đóng kết nối nếu cần
                }
            }
        }
    }
    
    @Override
    public void createPendingVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Kiểm tra xem suất chiếu có tồn tại không
            if (!veRepository.isSuatChieuExists(maSuatChieu)) {
                throw new SQLException("Suất chiếu không tồn tại: " + maSuatChieu);
            }

            // Lấy mã ghế từ số ghế và mã suất chiếu
            int maGhe = veRepository.getMaGheFromSoGhe(soGhe, maSuatChieu);
            if (maGhe <= 0) {
                throw new SQLException("Không tìm thấy ghế " + soGhe + " trong phòng chiếu");
            }

            // Kiểm tra xem có vé đã hủy cho ghế này không
            String checkCancelledSql = """
                SELECT maVe 
                FROM Ve 
                WHERE maSuatChieu = ? 
                AND maGhe = ? 
                AND trangThai = 'cancelled'""";
            
            PreparedStatement checkStmt = conn.prepareStatement(checkCancelledSql);
            checkStmt.setInt(1, maSuatChieu);
            checkStmt.setInt(2, maGhe);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Nếu có vé đã hủy, cập nhật lại trạng thái thay vì tạo mới
                int maVe = rs.getInt("maVe");
                String updateSql = "UPDATE Ve SET trangThai = ?, ngayDat = ?, maHoaDon = NULL WHERE maVe = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, TrangThaiVe.PENDING.toString());
                updateStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                updateStmt.setInt(3, maVe);
                updateStmt.executeUpdate();
                updateStmt.close();
                rs.close();
                checkStmt.close();
            } else {
                rs.close();
                checkStmt.close();
                
                // Kiểm tra xem ghế đã được đặt chưa (không bao gồm vé đã hủy)
                if (veRepository.isSeatTaken(maSuatChieu, soGhe)) {
                    throw new SQLException("Ghế " + soGhe + " đã được đặt cho suất chiếu " + maSuatChieu);
                }

                // Lấy mã giá vé từ mã ghế
                int maGiaVe = veRepository.getMaGiaVeFromMaGhe(maGhe);
                if (maGiaVe <= 0) {
                    throw new SQLException("Không tìm thấy giá vé cho ghế " + soGhe);
                }

                // Thêm vé mới với trạng thái PENDING
                String insertVeSql = "INSERT INTO Ve (maSuatChieu, maGhe, maGiaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement veStmt = conn.prepareStatement(insertVeSql);
                veStmt.setInt(1, maSuatChieu);
                veStmt.setInt(2, maGhe);
                veStmt.setInt(3, maGiaVe);
                veStmt.setString(4, TrangThaiVe.PENDING.toString());
                veStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                veStmt.executeUpdate();
                veStmt.close();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ghi log lỗi rollback nếu cần
                }
            }
            throw new SQLException("Lỗi khi tạo vé chờ thanh toán: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ghi log lỗi đóng kết nối nếu cần
                }
            }
        }
    }

    @Override
    public int confirmPayment(int maVe, int maKhachHang, int maNhanVien) throws SQLException {
        Connection conn = null;
        PreparedStatement hoaDonStmt = null;
        PreparedStatement updateVeStmt = null;
        PreparedStatement chiTietHoaDonStmt = null;
        ResultSet generatedKeys = null;
        int maHoaDonGenerated = 0;
        
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal giaVe = getGiaVeFromVe(maVe, conn);

            String hoaDonSql = "INSERT INTO HoaDon (maKhachHang, maNhanVien, ngayLap) VALUES (?, ?, NOW())";
            hoaDonStmt = conn.prepareStatement(hoaDonSql, PreparedStatement.RETURN_GENERATED_KEYS);
            hoaDonStmt.setInt(1, maKhachHang);
            if (maNhanVien == 0) {
                hoaDonStmt.setNull(2, Types.INTEGER); // Đặt maNhanVien thành NULL cho thanh toán online
            } else {
                hoaDonStmt.setInt(2, maNhanVien);
            }
            hoaDonStmt.executeUpdate();

            generatedKeys = hoaDonStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                maHoaDonGenerated = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy mã hóa đơn!");
            }
            
            String updateVeSql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
            updateVeStmt = conn.prepareStatement(updateVeSql);
            updateVeStmt.setString(1, TrangThaiVe.PAID.getValue());
            updateVeStmt.setInt(2, maHoaDonGenerated);
            updateVeStmt.setInt(3, maVe);
            int affectedRows = updateVeStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không tìm thấy vé với mã: " + maVe);
            }

            String chiTietHoaDonSql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
            chiTietHoaDonStmt = conn.prepareStatement(chiTietHoaDonSql);
            chiTietHoaDonStmt.setInt(1, maHoaDonGenerated);
            chiTietHoaDonStmt.setInt(2, maVe);
            chiTietHoaDonStmt.executeUpdate();

            conn.commit();
            return maHoaDonGenerated;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ghi log lỗi rollback nếu cần
                }
            }
            throw new SQLException("Lỗi khi xác nhận thanh toán: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (hoaDonStmt != null) try { hoaDonStmt.close(); } catch (SQLException e) {}
            if (updateVeStmt != null) try { updateVeStmt.close(); } catch (SQLException e) {}
            if (chiTietHoaDonStmt != null) try { chiTietHoaDonStmt.close(); } catch (SQLException e) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ghi log lỗi đóng kết nối nếu cần
                }
            }
        }
    }

    @Override
    public void cancelVe(int maVe) throws SQLException {
        Connection conn = null;
        PreparedStatement updateStmt = null;
        
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Kiểm tra trạng thái vé hiện tại
            String checkSql = "SELECT trangThai FROM Ve WHERE maVe = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, maVe);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy vé với mã: " + maVe);
                    }
                    
                    String trangThai = rs.getString("trangThai");
                    if ("paid".equalsIgnoreCase(trangThai)) {
                        throw new SQLException("Không thể hủy vé đã thanh toán");
                    }
                }
            }
            
            // Cập nhật trạng thái vé thành cancelled thay vì xóa
            String updateVeSql = "UPDATE Ve SET trangThai = 'cancelled' WHERE maVe = ?";
            updateStmt = conn.prepareStatement(updateVeSql);
            updateStmt.setInt(1, maVe);
            int affectedRows = updateStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không tìm thấy vé với mã: " + maVe);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ghi log lỗi rollback nếu cần
                }
            }
            throw new SQLException("Lỗi khi hủy vé: " + e.getMessage(), e);
        } finally {
            if (updateStmt != null) try { updateStmt.close(); } catch (SQLException e) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ghi log lỗi đóng kết nối nếu cần
                }
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
            
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("maVe");
            }
            throw new SQLException("Không tìm thấy mã vé vừa đặt!");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    @Override
    public int getPendingVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        String sql = """
            SELECT v.maVe 
            FROM Ve v
            JOIN Ghe g ON v.maGhe = g.maGhe
            WHERE v.maSuatChieu = ? 
            AND g.soGhe = ? 
            AND v.maHoaDon IS NULL 
            AND v.trangThai = 'pending'
            ORDER BY v.ngayDat DESC 
            LIMIT 1""";
            
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("maVe");
            }
            throw new SQLException("Không tìm thấy mã vé đang chờ thanh toán!");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
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
            
        try ( // Không đóng PreparedStatement và ResultSet khi Connection được truyền từ bên ngoài
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal("giaVe");
                    rs.close();
                    stmt.close();
                    return result;
                }
            }
        }
        throw new SQLException("Không tìm thấy giá vé cho mã vé: " + maVe);
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
            
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, maSuatChieu);
        stmt.setString(2, soGhe);
        ResultSet rs = stmt.executeQuery();
        boolean result = rs.next();
        rs.close();
        stmt.close();
        return result;
    }
}