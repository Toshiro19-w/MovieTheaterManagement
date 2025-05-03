package com.cinema.models.repositories;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VeRepository extends BaseRepository<Ve> {

    public VeRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Ve> findAll() throws SQLException {
        String sql = "SELECT * FROM Ve";
        List<Ve> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                LocalDateTime ngayDat = null;
                if (resultSet.getTimestamp("ngayDat") != null) {
                    ngayDat = resultSet.getTimestamp("ngayDat").toLocalDateTime();
                }

                result.add(new Ve(
                        resultSet.getInt("maVe"),
                        resultSet.getInt("maSuatChieu"),
                        resultSet.getInt("maPhong"),
                        resultSet.getString("soGhe"),
                        resultSet.getObject("maHoaDon", Integer.class),
                        resultSet.getBigDecimal("giaVe"),
                        TrangThaiVe.fromString(resultSet.getString("trangThai")),
                        ngayDat
                ));
            }
            return result;
        }
    }

    public List<Ve> findAllDetail() throws SQLException {
        String sql = """
                SELECT 
                    Ve.maVe,
                    Ve.trangThai,
                    Ve.giaVe,
                    Ve.soGhe,
                    Ve.ngayDat,
                    PhongChieu.tenPhong,
                    SuatChieu.ngayGioChieu,
                    Phim.tenPhim
                FROM Ve
                LEFT JOIN SuatChieu ON Ve.maSuatChieu = SuatChieu.maSuatChieu
                LEFT JOIN PhongChieu ON Ve.maPhong = PhongChieu.maPhong
                LEFT JOIN Phim ON SuatChieu.maPhim = Phim.maPhim
                ORDER BY Ve.maVe;""";

        List<Ve> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                LocalDateTime ngayDat = null, ngayGioChieu = null;
                if (resultSet.getTimestamp("ngayDat") != null) {
                    ngayDat = resultSet.getTimestamp("ngayDat").toLocalDateTime();
                }
                if (resultSet.getTimestamp("ngayGioChieu") != null) {
                    ngayGioChieu = resultSet.getTimestamp("ngayGioChieu").toLocalDateTime();
                }

                Ve ve = new Ve(
                        resultSet.getInt("maVe"),
                        TrangThaiVe.fromString(resultSet.getString("trangThai")),
                        resultSet.getBigDecimal("giaVe"),
                        resultSet.getString("soGhe"),
                        ngayDat,
                        resultSet.getString("tenPhong"),
                        ngayGioChieu,
                        resultSet.getString("tenPhim")
                );
                result.add(ve);
            }
        }
        return result;
    }

    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = """
                SELECT 
                    Ve.maVe,
                    Ve.trangThai,
                    Ve.giaVe,
                    Ve.soGhe,
                    Ve.ngayDat,
                    PhongChieu.tenPhong,
                    SuatChieu.ngayGioChieu,
                    Phim.tenPhim
                FROM Ve
                LEFT JOIN SuatChieu ON Ve.maSuatChieu = SuatChieu.maSuatChieu
                LEFT JOIN PhongChieu ON Ve.maPhong = PhongChieu.maPhong
                LEFT JOIN Phim ON SuatChieu.maPhim = Phim.maPhim
                WHERE soGhe = ?
                ORDER BY Ve.maVe;""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soGhe);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime ngayDat = null, ngayGioChieu = null;
                if (rs.getTimestamp("ngayDat") != null) {
                    ngayDat = rs.getTimestamp("ngayDat").toLocalDateTime();
                }
                if (rs.getTimestamp("ngayGioChieu") != null) {
                    ngayGioChieu = rs.getTimestamp("ngayGioChieu").toLocalDateTime();
                }

                Ve ve = new Ve(
                        rs.getInt("maVe"),
                        TrangThaiVe.fromString(rs.getString("trangThai")),
                        rs.getBigDecimal("giaVe"),
                        rs.getString("soGhe"),
                        ngayDat,
                        rs.getString("tenPhong"),
                        ngayGioChieu,
                        rs.getString("tenPhim")
                );
                veList.add(ve);
            }
        }
        return veList;
    }

    @Override
    public Ve save(Ve ve) throws SQLException {

        // Kiểm tra trùng lặp ghế
        if (isSeatTaken(ve.getMaSuatChieu(), ve.getSoGhe())) {
            throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
        }

        // Kiểm tra tính hợp lệ của maSuatChieu và maPhong

        if (isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }
        if (isPhongExists(ve.getMaPhong())) {
            throw new SQLException("Phòng chiếu với mã " + ve.getMaPhong() + " không tồn tại");
        }

        String sql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setObject(4, ve.getMaHoaDon(), Types.INTEGER);
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo vé thất bại, không có dòng nào được thêm.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ve.setMaVe(generatedKeys.getInt(1));
                    return ve;
                } else {
                    throw new SQLException("Tạo vé thất bại, không lấy được khóa chính.");
                }
            }
        }
    }

    @Override
    public Ve update(Ve ve) throws SQLException {

        // Kiểm tra trùng lặp ghế (trừ vé hiện tại)
        String checkSql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND trangThai != 'CANCELLED' AND maVe != ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, ve.getMaSuatChieu());
            checkStmt.setString(2, ve.getSoGhe());
            checkStmt.setInt(3, ve.getMaVe());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
            }
        }

        // Kiểm tra tính hợp lệ của maSuatChieu và maPhong

        if (isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }
        if (isPhongExists(ve.getMaPhong())) {
            throw new SQLException("Phòng chiếu với mã " + ve.getMaPhong() + " không tồn tại");
        }

        String sql = "UPDATE Ve SET maSuatChieu = ?, maPhong = ?, soGhe = ?, maHoaDon = ?, giaVe = ?, trangThai = ?, ngayDat = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setObject(4, ve.getMaHoaDon(), Types.INTEGER);
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);
            stmt.setInt(8, ve.getMaVe());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return ve;
            }
            return null;
        }
    }

    public void updateVeStatus(int maVe, String trangThai, Integer maHoaDon) throws SQLException {
        String sql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            stmt.setObject(2, maHoaDon, Types.INTEGER);
            stmt.setInt(3, maVe);
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        // Thay vì xóa, chuyển trạng thái thành CANCELLED
        String sql = "UPDATE Ve SET trangThai = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, TrangThaiVe.CANCELLED.toString());
            stmt.setInt(2, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không tìm thấy vé với mã: " + id);
            }
        }
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        Connection conn = null;
        PreparedStatement hoaDonStmt = null;
        PreparedStatement veStmt = null;
        PreparedStatement chiTietHoaDonStmt = null;

        PreparedStatement updateSuatChieuStmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            // Kiểm tra soSuatChieu
            String checkSuatChieuSql = "SELECT soSuatChieu FROM SuatChieu WHERE maSuatChieu = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSuatChieuSql)) {
                checkStmt.setInt(1, maSuatChieu);
                rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int soSuatChieu = rs.getInt("soSuatChieu");
                    if (soSuatChieu <= 0) {
                        throw new SQLException("Không còn suất chiếu khả dụng cho mã suất chiếu: " + maSuatChieu);
                    }
                } else {
                    throw new SQLException("Suất chiếu không tồn tại: " + maSuatChieu);
                }
            }

            // Kiểm tra ghế trống (không dựa vào AVAILABLE)
            if (isSeatTaken(maSuatChieu, soGhe)) {
                throw new SQLException("Ghế " + soGhe + " đã được đặt cho suất chiếu " + maSuatChieu);
            }

            // Tạo hóa đơn
            String hoaDonSql = "INSERT INTO HoaDon (maKhachHang, ngayLap, tongTien) VALUES (?, NOW(), ?)";
            hoaDonStmt = conn.prepareStatement(hoaDonSql, PreparedStatement.RETURN_GENERATED_KEYS);
            hoaDonStmt.setInt(1, maKhachHang);
            hoaDonStmt.setBigDecimal(2, giaVe);
            hoaDonStmt.executeUpdate();

            // Lấy maHoaDon
            try (ResultSet generatedKeys = hoaDonStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int maHoaDon = generatedKeys.getInt(1);
                    // Tạo vé mới với trạng thái BOOKED
                    String insertVeSql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    veStmt = conn.prepareStatement(insertVeSql, PreparedStatement.RETURN_GENERATED_KEYS);
                    veStmt.setInt(1, maSuatChieu);
                    veStmt.setInt(2, maPhong);
                    veStmt.setString(3, soGhe);
                    veStmt.setInt(4, maHoaDon);
                    veStmt.setBigDecimal(5, giaVe);
                    veStmt.setString(6, TrangThaiVe.BOOKED.toString());
                    veStmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                    veStmt.executeUpdate();

                    // Tạo ChiTietHoaDon
                    String chiTietHoaDonSql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
                    chiTietHoaDonStmt = conn.prepareStatement(chiTietHoaDonSql);
                    chiTietHoaDonStmt.setInt(1, maHoaDon);
                    try (ResultSet veKeys = veStmt.getGeneratedKeys()) {
                        if (veKeys.next()) {
                            int maVe = veKeys.getInt(1);
                            chiTietHoaDonStmt.setInt(2, maVe);
                            chiTietHoaDonStmt.executeUpdate();
                        }
                    }
                } else {
                    throw new SQLException("Không thể lấy mã hóa đơn!");
                }
            }

            // Giảm soSuatChieu
            String updateSuatChieuSql = "UPDATE SuatChieu SET soSuatChieu = soSuatChieu - 1 WHERE maSuatChieu = ?";
            updateSuatChieuStmt = conn.prepareStatement(updateSuatChieuSql);
            updateSuatChieuStmt.setInt(1, maSuatChieu);
            updateSuatChieuStmt.executeUpdate();

            conn.commit(); // Xác nhận giao dịch
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Hoàn tác nếu lỗi
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (hoaDonStmt != null) try { hoaDonStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (veStmt != null) try { veStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (chiTietHoaDonStmt != null) try { chiTietHoaDonStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (updateSuatChieuStmt != null) try { updateSuatChieuStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void confirmPayment(int maVe, int maHoaDon) throws SQLException {
        String sql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, TrangThaiVe.PAID.toString());
            stmt.setInt(2, maHoaDon);
            stmt.setInt(3, maVe);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không tìm thấy vé với mã: " + maVe);

        ResultSet generatedKeys = null;

        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Create HoaDon
            String hoaDonSql = "INSERT INTO HoaDon (maKhachHang, ngayLap, tongTien) VALUES (?, NOW(), ?)";
            hoaDonStmt = conn.prepareStatement(hoaDonSql, PreparedStatement.RETURN_GENERATED_KEYS);
            hoaDonStmt.setInt(1, maKhachHang);
            hoaDonStmt.setBigDecimal(2, giaVe); // tongTien is the ticket price for one seat
            hoaDonStmt.executeUpdate();

            // Get generated maHoaDon
            generatedKeys = hoaDonStmt.getGeneratedKeys();
            int maHoaDon;
            if (generatedKeys.next()) {
                maHoaDon = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy mã hóa đơn!");
            }

            // Create Ve
            Ve ve = new Ve(0, maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, TrangThaiVe.BOOKED, LocalDateTime.now());
            veStmt = conn.prepareStatement(
                    "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            veStmt.setInt(1, ve.getMaSuatChieu());
            veStmt.setInt(2, ve.getMaPhong());
            veStmt.setString(3, ve.getSoGhe());
            veStmt.setInt(4, ve.getMaHoaDon());
            veStmt.setBigDecimal(5, ve.getGiaVe());
            veStmt.setString(6, ve.getTrangThai().toString());
            veStmt.setTimestamp(7, java.sql.Timestamp.valueOf(ve.getNgayDat()));
            veStmt.executeUpdate();

            // Get generated maVe
            generatedKeys = veStmt.getGeneratedKeys();
            int maVe;
            if (generatedKeys.next()) {
                maVe = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy mã vé!");
            }

            // Create ChiTietHoaDon
            String chiTietHoaDonSql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
            chiTietHoaDonStmt = conn.prepareStatement(chiTietHoaDonSql);
            chiTietHoaDonStmt.setInt(1, maHoaDon);
            chiTietHoaDonStmt.setInt(2, maVe);
            chiTietHoaDonStmt.executeUpdate();

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (hoaDonStmt != null) try { hoaDonStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (veStmt != null) try { veStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (chiTietHoaDonStmt != null) try { chiTietHoaDonStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        String sql = "SELECT giaVe FROM Ve WHERE maSuatChieu = ? AND trangThai = 'available' LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }

            }
        }
        return null;
    }


    public BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        String sql = "SELECT giaVe FROM Ve WHERE maSuatChieu = ? LIMIT 1"; // Không cần kiểm tra AVAILABLE
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }

    private boolean isSuatChieuExists(int maSuatChieu) throws SQLException {
        String sql = "SELECT 1 FROM SuatChieu WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    private boolean isPhongExists(int maPhong) throws SQLException {
        String sql = "SELECT 1 FROM PhongChieu WHERE maPhong = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
        return null; // Có thể lấy giá từ SuatChieu nếu cần
    }

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        String sql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND maHoaDon IN " +
                "(SELECT maHoaDon FROM HoaDon WHERE maKhachHang = ?) ORDER BY ngayDat DESC LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            stmt.setInt(3, maKhachHang);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maVe");
            }
            throw new SQLException("Không tìm thấy mã vé vừa đặt!");
        }
    }

    public int getMaHoaDonFromVe(int maVe) throws SQLException {
        String sql = "SELECT maHoaDon FROM Ve WHERE maVe = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maHoaDon");
            }
            throw new SQLException("Không tìm thấy hóa đơn cho vé: " + maVe);
        }
    }

    private boolean isSuatChieuExists(int maSuatChieu) throws SQLException {
        String sql = "SELECT 1 FROM SuatChieu WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    private boolean isPhongExists(int maPhong) throws SQLException {
        String sql = "SELECT 1 FROM PhongChieu WHERE maPhong = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    private boolean isSeatTaken(int maSuatChieu, String soGhe) throws SQLException {
        String sql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND trangThai != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}